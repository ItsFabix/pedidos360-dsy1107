package cl.duoc.dsy1107.mensajeria.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.EXCHANGE_DIRECT;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.EXCHANGE_DLX;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.EXCHANGE_RETRY;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.EXCHANGE_TOPIC;

@Configuration
public class ExchangesConfig {

    /** Comandos dirigidos: la routing key coincide con el dominio. */
    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false);
    }

    /** Eventos por patrón: pedido.aceptado, pedido.cancelado, etc. */
    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC, true, false);
    }

    /** Recibe los mensajes que van a esperar su reintento. */
    @Bean
    DirectExchange retryExchange() {
        return new DirectExchange(EXCHANGE_RETRY, true, false);
    }

    /** Dead Letter Exchange: destino de los mensajes agotados o rechazados. */
    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }
}