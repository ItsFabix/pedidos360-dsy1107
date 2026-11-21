package cl.duoc.dsy1107.analitica.consumidor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import cl.duoc.dsy1107.analitica.modelo.PedidoEvento;
import cl.duoc.dsy1107.analitica.productor.AlertasPublisher;
import cl.duoc.dsy1107.analitica.servicio.AnaliticaServicio;

/**
 * Capa de mensajeria: recibe el evento, delega al servicio y publica la alerta
 * si hay.
 */
@Component
public class PedidoEventosConsumidor {

    private final AnaliticaServicio servicio;
    private final AlertasPublisher alertas;

    public PedidoEventosConsumidor(AnaliticaServicio servicio, AlertasPublisher alertas) {
        this.servicio = servicio;
        this.alertas = alertas;
    }

    @KafkaListener(topics = KafkaTopicos.EVENTOS, groupId = "pedidos360-analitica")
    public void alRecibir(ConsumerRecord<String, PedidoEvento> registro) {
        servicio.procesar(registro.value()).ifPresent(alertas::publicar);
    }
}