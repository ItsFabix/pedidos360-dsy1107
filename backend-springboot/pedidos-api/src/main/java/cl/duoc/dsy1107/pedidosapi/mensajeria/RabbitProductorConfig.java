package cl.duoc.dsy1107.pedidosapi.mensajeria;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitProductorConfig {

    public static final String EXCHANGE_DIRECT = "pedidos360.direct.exchange";
    public static final String EXCHANGE_TOPIC = "pedidos360.topic.exchange";

    /** Routing keys del exchange direct: una por dominio consumidor. */
    public static final String[] DOMINIOS = { "notificaciones", "cocina", "documentos" };

    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false);
    }

    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC, true, false);
    }
}