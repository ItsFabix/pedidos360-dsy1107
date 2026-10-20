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

/** Consumidor del dominio documentos. */
@Component
public class DocumentosConsumer {

    private static final Logger log = LoggerFactory.getLogger(DocumentosConsumer.class);

    private final ProcesadorEventos procesador;

    public DocumentosConsumer(ProcesadorEventos procesador) {
        this.procesador = procesador;
    }

    @RabbitListener(queues = RabbitNombres.COLA_DOCUMENTOS)
    public void recibir(Message mensaje, Channel canal) throws IOException {
        procesador.procesar(RabbitNombres.DOMINIO_DOCUMENTOS, mensaje, canal,
                evento -> log.info("Aviso enviado al cliente por el pedido {} ({})", evento.pedidoId(), evento.tipo()));
    }
}