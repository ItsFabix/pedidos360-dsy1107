package cl.duoc.dsy1107.mensajeria.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.COLA_COCINA;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.DLQ_COCINA;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.DOMINIO_COCINA;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.RETRY_COCINA;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.claveFallida;

/**
 * Cola principal, cola de reintento y DLQ del dominio cocina, con sus
 * bindings.
 */
@Configuration
public class CocinaTopologia {

    @Bean
    Queue cocinaQueue() {
        return Colas.principal(COLA_COCINA, DOMINIO_COCINA);
    }

    @Bean
    Queue cocinaRetryQueue(MensajeriaProperties propiedades) {
        return Colas.reintento(RETRY_COCINA, DOMINIO_COCINA, propiedades.retryTtlMs());
    }

    @Bean
    Queue cocinaDlq() {
        return Colas.muertos(DLQ_COCINA);
    }

    /** direct -> cola principal, con la routing key "cocina". */
    @Bean
    Binding cocinaBinding(@Qualifier("cocinaQueue") Queue cola,
            @Qualifier("directExchange") DirectExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(DOMINIO_COCINA);
    }

    /** retry exchange -> cola de espera. */
    @Bean
    Binding cocinaRetryBinding(@Qualifier("cocinaRetryQueue") Queue cola,
            @Qualifier("retryExchange") DirectExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(DOMINIO_COCINA);
    }

    /** DLX -> DLQ, con la routing key "cocina.failed". */
    @Bean
    Binding cocinaDlqBinding(@Qualifier("cocinaDlq") Queue cola,
            @Qualifier("deadLetterExchange") DirectExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(claveFallida(DOMINIO_COCINA));
    }
}