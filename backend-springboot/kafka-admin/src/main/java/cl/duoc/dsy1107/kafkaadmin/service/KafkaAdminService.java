package cl.duoc.dsy1107.kafkaadmin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.apache.kafka.common.errors.InvalidPartitionsException;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import cl.duoc.dsy1107.kafkaadmin.dto.CrearTopicoRequest;
import cl.duoc.dsy1107.kafkaadmin.dto.GrupoDetalle;
import cl.duoc.dsy1107.kafkaadmin.dto.GrupoResumen;
import cl.duoc.dsy1107.kafkaadmin.dto.LagParticion;
import cl.duoc.dsy1107.kafkaadmin.dto.ModificarTopicoRequest;
import cl.duoc.dsy1107.kafkaadmin.dto.ParticionInfo;
import cl.duoc.dsy1107.kafkaadmin.dto.TopicoDetalle;
import cl.duoc.dsy1107.kafkaadmin.dto.TopicoResumen;

/**
 * Logica de administracion de Kafka. Toda la comunicacion con el cluster pasa
 * por el cliente Admin
 * y cada llamada espera como maximo ESPERA_SEGUNDOS.
 *
 * Reglas del servicio:
 * - Los topicos pedidos360.* (incluidos los .DLT) y los internos (__*) no se
 * pueden eliminar.
 * - Las particiones solo se pueden aumentar (Kafka no permite reducirlas). Ojo:
 * al aumentarlas
 * cambia a que particion va cada clave nueva, asi que se pierde el orden entre
 * mensajes
 * viejos y nuevos de un mismo pedido.
 */
@Service
public class KafkaAdminService {

    private static final long ESPERA_SEGUNDOS = 10;
    private static final String PREFIJO_SISTEMA = "pedidos360.";
    private static final long RETENCION_POR_DEFECTO_MS = 604_800_000L; // 7 dias
    private static final List<String> CONFIGS_VISIBLES = List.of(
            "retention.ms", "cleanup.policy", "min.insync.replicas",
            "retention.bytes", "segment.bytes", "max.message.bytes");

    private final Admin admin;

    public KafkaAdminService(Admin admin) {
        this.admin = admin;
    }

    // ------------------------------------------------------------------ topicos

    public List<TopicoResumen> listarTopicos() {
        List<String> nombres = List.copyOf(esperar(admin.listTopics().names()));
        if (nombres.isEmpty()) {
            return List.of();
        }
        Map<String, TopicDescription> descripciones = esperar(admin.describeTopics(nombres).allTopicNames());
        return descripciones.values().stream()
                .sorted(Comparator.comparing(TopicDescription::name))
                .map(d -> new TopicoResumen(d.name(), d.partitions().size(), replicasDe(d)))
                .toList();
    }

    public TopicoDetalle describirTopico(String nombre) {
        TopicDescription descripcion = descripcionDe(nombre);

        ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        Config config = esperar(admin.describeConfigs(List.of(recurso)).all()).get(recurso);
        Map<String, String> visibles = new LinkedHashMap<>();
        for (String clave : CONFIGS_VISIBLES) {
            ConfigEntry entrada = config.get(clave);
            if (entrada != null) {
                visibles.put(clave, entrada.value());
            }
        }

        List<ParticionInfo> particiones = descripcion.partitions().stream()
                .map(p -> new ParticionInfo(
                        p.partition(),
                        p.leader() == null ? null : p.leader().id(),
                        idsDe(p.replicas()),
                        idsDe(p.isr())))
                .toList();

        return new TopicoDetalle(nombre, descripcion.partitions().size(), replicasDe(descripcion),
                visibles, particiones);
    }

    public TopicoResumen crearTopico(CrearTopicoRequest solicitud) {
        int particiones = solicitud.particiones() != null ? solicitud.particiones() : 3;
        int replicas = solicitud.replicas() != null ? solicitud.replicas() : 3;
        long retencion = solicitud.retencionMs() != null ? solicitud.retencionMs() : RETENCION_POR_DEFECTO_MS;

        // Con 3 replicas exige 2 en sincronia; con menos, el minimo posible.
        int minimoSincronizadas = Math.max(1, replicas - 1);

        NewTopic nuevo = new NewTopic(solicitud.nombre(), particiones, (short) replicas)
                .configs(Map.of(
                        "retention.ms", String.valueOf(retencion),
                        "min.insync.replicas", String.valueOf(minimoSincronizadas)));

        esperar(admin.createTopics(List.of(nuevo)).all());
        return new TopicoResumen(solicitud.nombre(), particiones, replicas);
    }

    public TopicoDetalle modificarTopico(String nombre, ModificarTopicoRequest solicitud) {
        if (solicitud.retencionMs() == null && solicitud.cleanupPolicy() == null
                && solicitud.particiones() == null) {
            throw new ReglaAdminException(HttpStatus.BAD_REQUEST,
                    "Debes enviar al menos un campo: retencionMs, cleanupPolicy o particiones");
        }

        // Valida todo ANTES de cambiar algo, para no dejar el topico a medio modificar.
        int particionesActuales = descripcionDe(nombre).partitions().size();
        if (solicitud.particiones() != null && solicitud.particiones() < particionesActuales) {
            throw new ReglaAdminException(HttpStatus.BAD_REQUEST,
                    "Kafka no permite reducir particiones: el topico tiene " + particionesActuales
                            + " y se pidieron " + solicitud.particiones());
        }

        Collection<AlterConfigOp> cambios = new ArrayList<>();
        if (solicitud.retencionMs() != null) {
            cambios.add(new AlterConfigOp(
                    new ConfigEntry("retention.ms", String.valueOf(solicitud.retencionMs())),
                    AlterConfigOp.OpType.SET));
        }
        if (solicitud.cleanupPolicy() != null) {
            cambios.add(new AlterConfigOp(
                    new ConfigEntry("cleanup.policy", solicitud.cleanupPolicy()),
                    AlterConfigOp.OpType.SET));
        }
        if (!cambios.isEmpty()) {
            ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
            esperar(admin.incrementalAlterConfigs(Map.of(recurso, cambios)).all());
        }

        if (solicitud.particiones() != null && solicitud.particiones() > particionesActuales) {
            esperar(admin.createPartitions(
                    Map.of(nombre, NewPartitions.increaseTo(solicitud.particiones()))).all());
        }

        return describirTopico(nombre);
    }

    public void eliminarTopico(String nombre) {
        if (nombre.startsWith(PREFIJO_SISTEMA) || nombre.startsWith("__")) {
            throw new ReglaAdminException(HttpStatus.CONFLICT,
                    "El topico " + nombre + " es del sistema y no se puede eliminar");
        }
        esperar(admin.deleteTopics(List.of(nombre)).all());
    }

    // ------------------------------------------------------------------- grupos

    public List<GrupoResumen> listarGrupos() {
        Collection<ConsumerGroupListing> listado = esperar(admin.listConsumerGroups().all());
        List<String> ids = listado.stream().map(ConsumerGroupListing::groupId).sorted().toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<String, ConsumerGroupDescription> descripciones = esperar(admin.describeConsumerGroups(ids).all());
        return ids.stream()
                .map(id -> {
                    ConsumerGroupDescription d = descripciones.get(id);
                    return new GrupoResumen(id, d.state().toString(), d.members().size());
                })
                .toList();
    }

    /**
     * lag por particion = ultimo offset del topico - ultimo offset confirmado por
     * el grupo.
     */
    public GrupoDetalle describirGrupo(String grupoId) {
        ConsumerGroupDescription descripcion = esperar(admin.describeConsumerGroups(List.of(grupoId)).all())
                .get(grupoId);
        if (descripcion == null || "DEAD".equalsIgnoreCase(descripcion.state().toString())) {
            throw new ReglaAdminException(HttpStatus.NOT_FOUND, "El grupo " + grupoId + " no existe");
        }

        Map<TopicPartition, OffsetAndMetadata> confirmados = esperar(
                admin.listConsumerGroupOffsets(grupoId).partitionsToOffsetAndMetadata());

        List<LagParticion> lags = new ArrayList<>();
        if (!confirmados.isEmpty()) {
            Map<TopicPartition, OffsetSpec> pedido = new HashMap<>();
            confirmados.keySet().forEach(tp -> pedido.put(tp, OffsetSpec.latest()));
            Map<TopicPartition, ListOffsetsResultInfo> finales = esperar(admin.listOffsets(pedido).all());

            confirmados.forEach((tp, confirmado) -> {
                if (confirmado == null) {
                    return;
                }
                long offsetFinal = finales.get(tp).offset();
                long lag = Math.max(0, offsetFinal - confirmado.offset());
                lags.add(new LagParticion(tp.topic(), tp.partition(), confirmado.offset(), offsetFinal, lag));
            });
            lags.sort(Comparator.comparing(LagParticion::topico).thenComparingInt(LagParticion::particion));
        }

        long lagTotal = lags.stream().mapToLong(LagParticion::lag).sum();
        return new GrupoDetalle(grupoId, descripcion.state().toString(), descripcion.members().size(),
                lagTotal, lags);
    }

    // ------------------------------------------------------------------ ayudas

    private TopicDescription descripcionDe(String nombre) {
        return esperar(admin.describeTopics(List.of(nombre)).allTopicNames()).get(nombre);
    }

    private int replicasDe(TopicDescription descripcion) {
        return descripcion.partitions().isEmpty() ? 0 : descripcion.partitions().get(0).replicas().size();
    }

    private List<Integer> idsDe(List<Node> nodos) {
        return nodos.stream().map(Node::id).toList();
    }

    /**
     * Espera el resultado de Kafka y traduce cualquier falla a una
     * ReglaAdminException.
     */
    private <T> T esperar(KafkaFuture<T> futuro) {
        try {
            return futuro.get(ESPERA_SEGUNDOS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReglaAdminException(HttpStatus.SERVICE_UNAVAILABLE, "La operacion fue interrumpida");
        } catch (TimeoutException e) {
            throw new ReglaAdminException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Kafka no respondio a tiempo. Revisa que el cluster este encendido");
        } catch (ExecutionException e) {
            throw traducir(e.getCause());
        }
    }

    private ReglaAdminException traducir(Throwable causa) {
        if (causa instanceof TopicExistsException) {
            return new ReglaAdminException(HttpStatus.CONFLICT, "El topico ya existe");
        }
        if (causa instanceof UnknownTopicOrPartitionException) {
            return new ReglaAdminException(HttpStatus.NOT_FOUND, "El topico no existe");
        }
        if (causa instanceof GroupIdNotFoundException) {
            return new ReglaAdminException(HttpStatus.NOT_FOUND, "El grupo no existe");
        }
        if (causa instanceof InvalidReplicationFactorException) {
            return new ReglaAdminException(HttpStatus.BAD_REQUEST,
                    "No hay brokers suficientes para esa cantidad de replicas");
        }
        if (causa instanceof InvalidPartitionsException || causa instanceof InvalidTopicException
                || causa instanceof InvalidConfigurationException) {
            return new ReglaAdminException(HttpStatus.BAD_REQUEST, causa.getMessage());
        }
        if (causa instanceof org.apache.kafka.common.errors.TimeoutException) {
            return new ReglaAdminException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Kafka no respondio a tiempo. Revisa que el cluster este encendido");
        }
        return new ReglaAdminException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error de Kafka: " + causa.getClass().getSimpleName());
    }
}