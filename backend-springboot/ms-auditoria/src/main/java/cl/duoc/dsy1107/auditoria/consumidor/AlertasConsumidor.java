package cl.duoc.dsy1107.auditoria.consumidor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import cl.duoc.dsy1107.auditoria.modelo.AlertaPedido;
import cl.duoc.dsy1107.auditoria.modelo.RegistroAlerta;
import cl.duoc.dsy1107.auditoria.servicio.AuditoriaServicio;

/**
 * Segundo consumidor: lee el topico de alertas. Grupo propio, y tipo de mensaje
 * distinto al de eventos, por eso sobreescribe el tipo por defecto del JSON.
 */
@Component
public class AlertasConsumidor {

    private final AuditoriaServicio servicio;

    public AlertasConsumidor(AuditoriaServicio servicio) {
        this.servicio = servicio;
    }

    @KafkaListener(topics = KafkaTopicos.ALERTAS, groupId = "pedidos360-auditoria-alertas", properties = "spring.json.value.default.type=cl.duoc.dsy1107.auditoria.modelo.AlertaPedido")
    public void alRecibir(ConsumerRecord<String, AlertaPedido> registro) {
        servicio.registrarAlerta(new RegistroAlerta(
                registro.value(), registro.partition(), registro.offset()));
    }
}