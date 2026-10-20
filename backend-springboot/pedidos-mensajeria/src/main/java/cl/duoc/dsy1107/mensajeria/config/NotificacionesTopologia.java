package cl.duoc.dsy1107.mensajeria.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.COLA_NOTIFICACIONES;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.DLQ_NOTIFICACIONES;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.DOMINIO_NOTIFICACIONES;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.RETRY_NOTIFICACIONES;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.claveFallida;

/**
 * Cola principal, cola de reintento y DLQ del dominio notificaciones, con sus
 * bindings.
 */
@Configuration
public class NotificacionesTopologia {

    @Bean
    Queue notificacionesQueue() {
        return Colas.principal(COLA_NOTIFICACIONES, DOMINIO_NOTIFICACIONES);
    }

    @Bean
    Queue notificacionesRetryQueue(MensajeriaProperties propiedades) {
        return Colas.reintento(RETRY_NOTIFICACIONES, DOMINIO_NOTIFICACIONES, propiedades.retryTtlMs());
    }

    @Bean
    Queue notificacionesDlq() {
        return Colas.muertos(DLQ_NOTIFICACIONES);
    }

    /** direct -> cola principal, con la routing key "notificaciones". */
    @Bean
    Binding notificacionesBinding(@Qualifier("notificacionesQueue") Queue cola,
            @Qualifier("directExchange") DirectExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(DOMINIO_NOTIFICACIONES);
    }

    /** retry exchange -> cola de espera. */
    @Bean
    Binding notificacionesRetryBinding(@Qualifier("notificacionesRetryQueue") Queue cola,
            @Qualifier("retryExchange") DirectExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(DOMINIO_NOTIFICACIONES);
    }

    /** DLX -> DLQ, con la routing key "notificaciones.failed". */
    @Bean
    Binding notificacionesDlqBinding(@Qualifier("notificacionesDlq") Queue cola,
            @Qualifier("deadLetterExchange") DirectExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(claveFallida(DOMINIO_NOTIFICACIONES));
    }
}