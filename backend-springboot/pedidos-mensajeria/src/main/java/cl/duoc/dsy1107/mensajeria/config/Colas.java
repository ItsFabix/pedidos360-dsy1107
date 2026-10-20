package cl.duoc.dsy1107.mensajeria.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;

import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.EXCHANGE_DIRECT;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.EXCHANGE_DLX;
import static cl.duoc.dsy1107.mensajeria.config.RabbitNombres.claveFallida;

final class Colas {

    /**
     * Cola de trabajo: si un mensaje se rechaza sin requeue, el broker lo manda al
     * DLX.
     */
    static Queue principal(String nombre, String dominio) {
        return QueueBuilder.durable(nombre)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(claveFallida(dominio))
                .build();
    }

    /**
     * Cola de espera: el mensaje descansa ttlMs y el broker lo devuelve a la cola
     * principal.
     */
    static Queue reintento(String nombre, String dominio, long ttlMs) {
        return QueueBuilder.durable(nombre)
                .ttl((int) ttlMs)
                .deadLetterExchange(EXCHANGE_DIRECT)
                .deadLetterRoutingKey(dominio)
                .build();
    }

    /**
     * Cola de mensajes fallidos. Sin TTL: se conservan hasta que alguien los
     * revise.
     */
    static Queue muertos(String nombre) {
        return QueueBuilder.durable(nombre).build();
    }

    private Colas() {
    }
}