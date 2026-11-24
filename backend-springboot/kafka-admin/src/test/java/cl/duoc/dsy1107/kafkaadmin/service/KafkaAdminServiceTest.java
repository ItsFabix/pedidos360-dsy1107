package cl.duoc.dsy1107.kafkaadmin.service;

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;

import cl.duoc.dsy1107.kafkaadmin.dto.ModificarTopicoRequest;

class KafkaAdminServiceTest {

    private final Admin admin = mock(Admin.class);
    private final KafkaAdminService servicio = new KafkaAdminService(admin);

    /**
     * Ejecuta la accion y devuelve el codigo HTTP de la regla incumplida (null si
     * no hubo error).
     */
    private HttpStatus estadoDelError(Runnable accion) {
        try {
            accion.run();
        } catch (ReglaAdminException e) {
            return e.getEstado();
        }
        return null;
    }

    @Test
    void noPermiteEliminarTopicosDelSistema() {
        assertThat(estadoDelError(() -> servicio.eliminarTopico("pedidos360.pedidos.eventos")))
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(estadoDelError(() -> servicio.eliminarTopico("pedidos360.pedidos.eventos.DLT")))
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(estadoDelError(() -> servicio.eliminarTopico("__consumer_offsets")))
                .isEqualTo(HttpStatus.CONFLICT);

        // Se rechaza antes de hablar con Kafka.
        verifyNoInteractions(admin);
    }

    @Test
    void exigeAlMenosUnCampoParaModificar() {
        var vacio = new ModificarTopicoRequest(null, null, null);

        assertThat(estadoDelError(() -> servicio.modificarTopico("cualquiera", vacio)))
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(admin);
    }

    @Test
    void noPermiteReducirParticiones() {
        Node nodo = new Node(1, "localhost", 9092);
        List<TopicPartitionInfo> particiones = List.of(
                new TopicPartitionInfo(0, nodo, List.of(nodo), List.of(nodo)),
                new TopicPartitionInfo(1, nodo, List.of(nodo), List.of(nodo)),
                new TopicPartitionInfo(2, nodo, List.of(nodo), List.of(nodo)));
        TopicDescription descripcion = new TopicDescription("prueba", false, particiones);

        DescribeTopicsResult resultado = mock(DescribeTopicsResult.class);
        when(resultado.allTopicNames()).thenReturn(KafkaFuture.completedFuture(Map.of("prueba", descripcion)));
        when(admin.describeTopics(anyCollection())).thenReturn(resultado);

        var reducir = new ModificarTopicoRequest(null, null, 2);

        assertThat(estadoDelError(() -> servicio.modificarTopico("prueba", reducir)))
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}