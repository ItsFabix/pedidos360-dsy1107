package cl.duoc.dsy1107.mensajeria.consumer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import cl.duoc.dsy1107.mensajeria.config.RabbitNombres;
import cl.duoc.dsy1107.mensajeria.service.ProcesadorEventos;

/** Consumidor del dominio cocina. */
@Component
public class CocinaConsumer {

    private static final Logger log = LoggerFactory.getLogger(CocinaConsumer.class);

    private final ProcesadorEventos procesador;

    public CocinaConsumer(ProcesadorEventos procesador) {
        this.procesador = procesador;
    }

    @RabbitListener(queues = RabbitNombres.COLA_COCINA)
    public void recibir(Message mensaje, Channel canal) throws IOException {
        procesador.procesar(RabbitNombres.DOMINIO_COCINA, mensaje, canal,
                evento -> log.info("Ticket de cocina generado para el pedido {} ({})", evento.pedidoId(),
                        evento.tipo()));
    }
}