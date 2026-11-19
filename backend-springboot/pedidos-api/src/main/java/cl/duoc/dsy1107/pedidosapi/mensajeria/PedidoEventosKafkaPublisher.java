package cl.duoc.dsy1107.pedidosapi.mensajeria;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Productor Kafka. Igual que el de RabbitMQ, publica solo cuando la transaccion
 * de la base de datos ya se confirmo (AFTER_COMMIT).
 *
 * Cada cambio de estado genera un evento en el topico de eventos. La key es el
 * id del pedido: asi todos los eventos de un mismo pedido caen en la misma
 * particion y se conservan en orden.
 *
 * Solo se activa con pedidos360.kafka.habilitado=true (en la EC2 no hay Kafka).
 * Limite conocido: igual que con RabbitMQ, si Kafka no responde el pedido queda
 * guardado y el evento se pierde (queda en el log). La solucion completa seria
 * el patron outbox.
 */
@Component
@ConditionalOnProperty(name = "pedidos360.kafka.habilitado", havingValue = "true")
public class PedidoEventosKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventosKafkaPublisher.class);

    private final KafkaTemplate<String, PedidoEvento> kafka;

    public PedidoEventosKafkaPublisher(KafkaTemplate<String, PedidoEvento> kafka) {
        this.kafka = kafka;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiarEstado(PedidoEstadoCambiado cambio) {
        PedidoEvento evento = new PedidoEvento(
                UUID.randomUUID().toString(),
                cambio.pedidoId(),
                cambio.clienteId(),
                "PEDIDO_" + cambio.estado().name(),
                OffsetDateTime.now().toString());
        String clave = String.valueOf(cambio.pedidoId());

        try {
            kafka.send(KafkaTopicos.EVENTOS, clave, evento).whenComplete((resultado, error) -> {
                if (error != null) {
                    log.error("Kafka NO confirmo el evento {} del pedido {}: {}",
                            evento.eventoId(), cambio.pedidoId(), error.getMessage());
                } else {
                    RecordMetadata meta = resultado.getRecordMetadata();
                    log.info("Evento {} publicado en Kafka: topico={} key={} particion={} offset={}",
                            evento.eventoId(), meta.topic(), clave, meta.partition(), meta.offset());
                }
            });
        } catch (RuntimeException e) {
            log.error("No se pudo publicar en Kafka el evento del pedido {} ({}): {}",
                    cambio.pedidoId(), cambio.estado(), e.getMessage());
        }
    }
}