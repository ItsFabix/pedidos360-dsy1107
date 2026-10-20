package cl.duoc.dsy1107.mensajeria.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.COLA_DOCUMENTOS;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.DLQ_DOCUMENTOS;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.DOMINIO_DOCUMENTOS;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.RETRY_DOCUMENTOS;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.claveFallida;

/**
 * Cola principal, cola de reintento y DLQ del dominio documentos, con sus
 * bindings.
 */
@Configuration
public class DocumentosTopologia {

    @Bean
    Queue documentosQueue() {
        return Colas.principal(COLA_DOCUMENTOS, DOMINIO_DOCUMENTOS);
    }

    @Bean
    Queue documentosRetryQueue(MensajeriaProperties propiedades) {
        return Colas.reintento(RETRY_DOCUMENTOS, DOMINIO_DOCUMENTOS, propiedades.retryTtlMs());
    }

    @Bean
    Queue documentosDlq() {
        return Colas.muertos(DLQ_DOCUMENTOS);
    }

    /** direct -> cola principal, con la routing key "documentos". */
    @Bean
    Binding documentosBinding(@Qualifier("documentosQueue") Queue cola,
            @Qualifier("directExchange") DirectExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(DOMINIO_DOCUMENTOS);
    }

    /** retry exchange -> cola de espera. */
    @Bean
    Binding documentosRetryBinding(@Qualifier("documentosRetryQueue") Queue cola,
            @Qualifier("retryExchange") DirectExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(DOMINIO_DOCUMENTOS);
    }

    /** DLX -> DLQ, con la routing key "documentos.failed". */
    @Bean
    Binding documentosDlqBinding(@Qualifier("documentosDlq") Queue cola,
            @Qualifier("deadLetterExchange") DirectExchange exchange) {
        return BindingBuilder.bind(cola).to(exchange).with(claveFallida(DOMINIO_DOCUMENTOS));
    }
}