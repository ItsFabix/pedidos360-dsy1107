package cl.duoc.dsy1107.mensajeria.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.COLA_MONITOREO;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.PATRON_TOPIC_PEDIDO;

/**
 * Caso de uso del exchange topic: una cola que observa todos los eventos
 * pedido.*
 */
@Configuration
public class MonitoreoTopologia {

    @Bean
    Queue monitoreoQueue() {
        return QueueBuilder.durable(COLA_MONITOREO).build();
    }

    @Bean
    Binding monitoreoBinding(@Qualifier("monitoreoQueue") Queue cola,
            @Qualifier("topicExchange") TopicExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(PATRON_TOPIC_PEDIDO);
    }
}