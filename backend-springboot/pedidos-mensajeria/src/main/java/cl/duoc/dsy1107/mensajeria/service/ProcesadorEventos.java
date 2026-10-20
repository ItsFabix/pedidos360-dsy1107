package cl.duoc.dsy1107.mensajeria.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import cl.duoc.dsy1107.mensajeria.config.MensajeriaProperties;
import cl.duoc.dsy1107.mensajeria.config.RabbitNombres;
import cl.duoc.dsy1107.mensajeria.model.PedidoEvento;

/**
 * Política de confirmación y manejo de errores de todos los consumidores.
 *
 * Éxito: basicAck. El broker borra el mensaje.
 * Mensaje ilegible (JSON inválido): no tiene sentido reintentarlo. basicNack
 * sin requeue
 * y el broker lo envía directo a la DLQ.
 * Error de procesamiento con intentos disponibles: se publica una copia en la
 * cola .retry
 * (espera el TTL y vuelve a la principal) y se hace ack del original.
 * Error de procesamiento con intentos agotados: basicNack sin requeue y el
 * broker lo envía
 * a la DLQ del dominio.
 * No se pudo publicar el reintento: basicNack con requeue, para no perder el
 * mensaje.
 */
@Service
public class ProcesadorEventos {

    private static final Logger log = LoggerFactory.getLogger(ProcesadorEventos.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;
    private final MensajeriaProperties propiedades;
    private final SimulacionFallos simulacion;

    public ProcesadorEventos(RabbitTemplate rabbitTemplate, ObjectMapper mapper,
            MensajeriaProperties propiedades, SimulacionFallos simulacion) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
        this.propiedades = propiedades;
        this.simulacion = simulacion;
    }

    public void procesar(String dominio, Message mensaje, Channel canal, AccionDominio accion) throws IOException {
        long tag = mensaje.getMessageProperties().getDeliveryTag();
        int intentos = leerIntentos(mensaje);

        PedidoEvento evento;
        try {
            evento = mapper.readValue(mensaje.getBody(), PedidoEvento.class);
        } catch (IOException e) {
            log.error("[{}] Mensaje ilegible ({}), se envía a la DLQ sin reintentos", dominio,
                    e.getClass().getSimpleName());
            canal.basicNack(tag, false, false);
            return;
        }

        try {
            simulacion.verificar(dominio);
            accion.ejecutar(evento);
        } catch (Exception error) {
            manejarError(dominio, mensaje, canal, tag, intentos, evento, error);
            return;
        }

        canal.basicAck(tag, false);
        log.info("[{}] ACK pedido={} evento={} intento={}", dominio, evento.pedidoId(), evento.eventoId(), intentos);
    }

    private void manejarError(String dominio, Message mensaje, Channel canal, long tag, int intentos,
            PedidoEvento evento, Exception error) throws IOException {

        log.warn("[{}] Error procesando pedido={} intento={}: {}", dominio, evento.pedidoId(), intentos,
                error.getMessage());

        if (intentos >= propiedades.reintentosMax()) {
            log.error("[{}] Intentos agotados para pedido={}. NACK sin requeue, el mensaje pasa a la DLQ",
                    dominio, evento.pedidoId());
            canal.basicNack(tag, false, false);
            return;
        }

        try {
            rabbitTemplate.send(RabbitNombres.EXCHANGE_RETRY, dominio, copiaParaReintento(mensaje, intentos + 1));
            canal.basicAck(tag, false);
            log.info("[{}] Pedido={} enviado a la cola de reintento ({} ms), reintento {} de {}",
                    dominio, evento.pedidoId(), propiedades.retryTtlMs(), intentos + 1, propiedades.reintentosMax());
        } catch (Exception falloPublicacion) {
            log.error("[{}] No se pudo publicar el reintento ({}). NACK con requeue para no perder el mensaje",
                    dominio, falloPublicacion.getMessage());
            canal.basicNack(tag, false, true);
        }
    }

    private Message copiaParaReintento(Message original, int nuevoIntento) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        props.setHeader(RabbitNombres.HEADER_INTENTOS, nuevoIntento);
        return new Message(original.getBody(), props);
    }

    private int leerIntentos(Message mensaje) {
        Object valor = mensaje.getMessageProperties().getHeaders().get(RabbitNombres.HEADER_INTENTOS);
        return valor instanceof Number n ? n.intValue() : 0;
    }
}