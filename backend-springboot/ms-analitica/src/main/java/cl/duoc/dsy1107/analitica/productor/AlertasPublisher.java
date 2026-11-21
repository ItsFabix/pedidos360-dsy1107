package cl.duoc.dsy1107.analitica.productor;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import cl.duoc.dsy1107.analitica.consumidor.KafkaTopicos;
import cl.duoc.dsy1107.analitica.modelo.AlertaPedido;

/** Productor: publica las alertas en el segundo topico (key = pedidoId). */
@Component
public class AlertasPublisher {

    private static final Logger log = LoggerFactory.getLogger(AlertasPublisher.class);

    private final KafkaTemplate<String, AlertaPedido> kafka;

    public AlertasPublisher(KafkaTemplate<String, AlertaPedido> kafka) {
        this.kafka = kafka;
    }

    public void publicar(AlertaPedido alerta) {
        String clave = String.valueOf(alerta.pedidoId());
        kafka.send(KafkaTopicos.ALERTAS, clave, alerta).whenComplete((resultado, error) -> {
            if (error != null) {
                log.error("Kafka NO confirmo la alerta {} del pedido {}: {}",
                        alerta.alertaId(), alerta.pedidoId(), error.getMessage());
            } else {
                RecordMetadata meta = resultado.getRecordMetadata();
                log.info("Alerta {} publicada en Kafka: topico={} key={} particion={} offset={}",
                        alerta.alertaId(), meta.topic(), clave, meta.partition(), meta.offset());
            }
        });
    }
}