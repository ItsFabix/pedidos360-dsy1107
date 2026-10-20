package cl.duoc.dsy1107.pedidosapi.mensajeria;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.dsy1107.pedidosapi.model.EstadoPedido;

/**
 * Productor RabbitMQ. Escucha el evento interno y publica SOLO cuando la
 * transacción de la base de datos
 * ya se confirmó (AFTER_COMMIT): si el pedido se revierte, no sale ningún
 * mensaje.
 *
 * Límite conocido: si RabbitMQ no responde en ese instante, el pedido queda
 * guardado y el mensaje
 * se pierde (queda en el log). La solución completa sería el patrón outbox.
 */
@Component
@ConditionalOnProperty(name = "pedidos360.mensajeria.habilitado", havingValue = "true", matchIfMissing = true)
public class PedidoEventosPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventosPublisher.class);

    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;

    public PedidoEventosPublisher(RabbitTemplate rabbit, ObjectMapper mapper) {
        this.rabbit = rabbit;
        this.mapper = mapper;
        this.rabbit.setConfirmCallback((correlacion, confirmado, causa) -> {
            String id = correlacion != null ? correlacion.getId() : "-";
            if (confirmado) {
                log.debug("Broker confirmó la recepción del mensaje {}", id);
            } else {
                log.error("Broker NO confirmó el mensaje {}: {}", id, causa);
            }
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiarEstado(PedidoEstadoCambiado cambio) {
        try {
            PedidoEvento evento = new PedidoEvento(
                    UUID.randomUUID().toString(),
                    cambio.pedidoId(),
                    cambio.clienteId(),
                    "PEDIDO_" + cambio.estado().name(),
                    OffsetDateTime.now().toString());
            String json = mapper.writeValueAsString(evento);

            // Un pedido aceptado dispara trabajo en los tres dominios (exchange direct).
            if (cambio.estado() == EstadoPedido.ACEPTADO) {
                for (String dominio : RabbitProductorConfig.DOMINIOS) {
                    enviar(RabbitProductorConfig.EXCHANGE_DIRECT, dominio, json, evento.eventoId());
                }
            }

            // Todo cambio de estado se anuncia por el exchange topic: pedido.aceptado,
            // pedido.cancelado, ...
            enviar(RabbitProductorConfig.EXCHANGE_TOPIC, claveTopic(cambio.estado()), json, evento.eventoId());

        } catch (JsonProcessingException | RuntimeException e) {
            log.error("No se pudo publicar el evento del pedido {} ({}): {}",
                    cambio.pedidoId(), cambio.estado(), e.getMessage());
        }
    }

    private void enviar(String exchange, String routingKey, String json, String eventoId) {
        rabbit.convertAndSend(exchange, routingKey, json, mensaje -> {
            mensaje.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
            mensaje.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return mensaje;
        }, new CorrelationData(eventoId + ":" + routingKey));
        log.info("Evento {} publicado en {} con routing key '{}'", eventoId, exchange, routingKey);
    }

    /**
     * EN_PREPARACION -> pedido.enpreparacion (el patrón pedido.* solo admite una
     * palabra).
     */
    private String claveTopic(EstadoPedido estado) {
        return "pedido." + estado.name().toLowerCase().replace("_", "");
    }
}