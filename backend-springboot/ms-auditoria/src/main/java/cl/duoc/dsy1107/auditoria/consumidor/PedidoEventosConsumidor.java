package cl.duoc.dsy1107.auditoria.consumidor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import cl.duoc.dsy1107.auditoria.modelo.PedidoEvento;
import cl.duoc.dsy1107.auditoria.modelo.RegistroAuditoria;
import cl.duoc.dsy1107.auditoria.servicio.AuditoriaServicio;

/**
 * Capa de mensajeria: recibe el registro de Kafka y lo pasa al servicio.
 * La logica de negocio vive en AuditoriaServicio.
 */
@Component
public class PedidoEventosConsumidor {

    private final AuditoriaServicio servicio;

    public PedidoEventosConsumidor(AuditoriaServicio servicio) {
        this.servicio = servicio;
    }

    @KafkaListener(topics = KafkaTopicos.EVENTOS, groupId = "pedidos360-auditoria")
    public void alRecibir(ConsumerRecord<String, PedidoEvento> registro) {
        servicio.registrar(new RegistroAuditoria(
                registro.value(), registro.partition(), registro.offset()));
    }
}