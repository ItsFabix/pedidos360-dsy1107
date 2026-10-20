package cl.duoc.dsy1107.mensajeria.consumer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import cl.duoc.dsy1107.mensajeria.config.RabbitNombres;

/** Consumidor del exchange topic: registra todos los eventos pedido.* */
@Component
public class MonitoreoConsumer {

    private static final Logger log = LoggerFactory.getLogger(MonitoreoConsumer.class);

    @RabbitListener(queues = RabbitNombres.COLA_MONITOREO)
    public void observar(Message mensaje, Channel canal) throws IOException {
        log.info("[topic] routingKey={}", mensaje.getMessageProperties().getReceivedRoutingKey());
        canal.basicAck(mensaje.getMessageProperties().getDeliveryTag(), false);
    }
}