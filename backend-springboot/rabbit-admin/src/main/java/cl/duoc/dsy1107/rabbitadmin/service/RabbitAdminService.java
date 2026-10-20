package cl.duoc.dsy1107.rabbitadmin.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import cl.duoc.dsy1107.rabbitadmin.dto.ColaInfo;
import cl.duoc.dsy1107.rabbitadmin.dto.CrearBindingRequest;
import cl.duoc.dsy1107.rabbitadmin.dto.CrearColaRequest;
import cl.duoc.dsy1107.rabbitadmin.dto.CrearExchangeRequest;

/**
 * Toda la lógica de administración del broker vive aquí; el controlador solo
 * traduce HTTP.
 *
 * Reglas:
 * - Los nombres solo admiten letras, números, punto, guion y guion bajo (máx.
 * 100).
 * - El prefijo "amq." está reservado por RabbitMQ.
 * - Lo que empieza con "pedidos360." es la topología del sistema: se puede
 * consultar, pero no eliminar.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private static final Pattern NOMBRE = Pattern.compile("^[A-Za-z0-9._-]{1,100}$");
    private static final Pattern ROUTING_KEY = Pattern.compile("^[A-Za-z0-9._*#-]{0,255}$");
    private static final String PREFIJO_RESERVADO = "amq.";
    private static final String PREFIJO_SISTEMA = "pedidos360.";

    private final AmqpAdmin admin;

    public RabbitAdminService(AmqpAdmin admin) {
        this.admin = admin;
    }

    // ------------------------------------------------------------------ colas

    public ColaInfo crearCola(CrearColaRequest request) {
        String nombre = validarNombre(request.nombre(), "cola");
        noReservado(nombre);

        Map<String, Object> argumentos = new HashMap<>();
        if (request.ttlMs() != null) {
            argumentos.put("x-message-ttl", request.ttlMs());
        }
        if (request.maxLength() != null) {
            argumentos.put("x-max-length", request.maxLength());
        }
        if (request.deadLetterExchange() != null && !request.deadLetterExchange().isBlank()) {
            argumentos.put("x-dead-letter-exchange", validarNombre(request.deadLetterExchange(), "exchange"));
        }
        if (request.deadLetterRoutingKey() != null && !request.deadLetterRoutingKey().isBlank()) {
            argumentos.put("x-dead-letter-routing-key", validarRoutingKey(request.deadLetterRoutingKey()));
        }

        boolean durable = request.durable() == null || request.durable();
        admin.declareQueue(new Queue(nombre, durable, false, false, argumentos));
        log.info("Cola creada: {}", nombre);

        return consultarCola(nombre);
    }

    public ColaInfo consultarCola(String nombre) {
        validarNombre(nombre, "cola");
        QueueInformation info = admin.getQueueInfo(nombre);
        if (info == null) {
            throw new ReglaAdminException(HttpStatus.NOT_FOUND, "La cola '" + nombre + "' no existe");
        }
        return new ColaInfo(info.getName(), info.getMessageCount(), info.getConsumerCount());
    }

    public void eliminarCola(String nombre) {
        validarNombre(nombre, "cola");
        noReservado(nombre);
        noSistema(nombre);
        consultarCola(nombre); // lanza 404 si no existe
        admin.deleteQueue(nombre);
        log.info("Cola eliminada: {}", nombre);
    }

    // -------------------------------------------------------------- exchanges

    public void crearExchange(CrearExchangeRequest request) {
        String nombre = validarNombre(request.nombre(), "exchange");
        noReservado(nombre);

        boolean durable = request.durable() == null || request.durable();
        Exchange exchange = switch (request.tipo()) {
            case DIRECT -> new DirectExchange(nombre, durable, false);
            case TOPIC -> new TopicExchange(nombre, durable, false);
            case FANOUT -> new FanoutExchange(nombre, durable, false);
        };
        admin.declareExchange(exchange);
        log.info("Exchange creado: {} ({})", nombre, request.tipo());
    }

    public void eliminarExchange(String nombre) {
        validarNombre(nombre, "exchange");
        noReservado(nombre);
        noSistema(nombre);
        admin.deleteExchange(nombre);
        log.info("Exchange eliminado: {}", nombre);
    }

    // --------------------------------------------------------------- bindings

    public void crearBinding(CrearBindingRequest request) {
        Binding binding = construirBinding(request.cola(), request.exchange(), request.routingKey());
        consultarCola(request.cola()); // lanza 404 si la cola no existe
        admin.declareBinding(binding);
        log.info("Binding creado: {} -> {} ({})", request.exchange(), request.cola(), binding.getRoutingKey());
    }

    public void eliminarBinding(String cola, String exchange, String routingKey) {
        Binding binding = construirBinding(cola, exchange, routingKey);
        if (cola.startsWith(PREFIJO_SISTEMA) || exchange.startsWith(PREFIJO_SISTEMA)) {
            throw new ReglaAdminException(HttpStatus.CONFLICT,
                    "Los bindings de la topología del sistema (pedidos360.*) no se pueden eliminar");
        }
        admin.removeBinding(binding);
        log.info("Binding eliminado: {} -> {} ({})", exchange, cola, binding.getRoutingKey());
    }

    // ------------------------------------------------------------ validaciones

    private Binding construirBinding(String cola, String exchange, String routingKey) {
        validarNombre(cola, "cola");
        validarNombre(exchange, "exchange");
        String clave = validarRoutingKey(routingKey == null ? "" : routingKey);
        return new Binding(cola, Binding.DestinationType.QUEUE, exchange, clave, null);
    }

    private String validarNombre(String nombre, String que) {
        if (nombre == null || !NOMBRE.matcher(nombre).matches()) {
            throw new ReglaAdminException(HttpStatus.BAD_REQUEST,
                    "Nombre de " + que + " inválido: use letras, números, punto, guion o guion bajo (máx. 100)");
        }
        return nombre;
    }

    private String validarRoutingKey(String clave) {
        if (!ROUTING_KEY.matcher(clave).matches()) {
            throw new ReglaAdminException(HttpStatus.BAD_REQUEST,
                    "Routing key inválida: use letras, números, punto, guion, guion bajo, * o #");
        }
        return clave;
    }

    private void noReservado(String nombre) {
        if (nombre.startsWith(PREFIJO_RESERVADO)) {
            throw new ReglaAdminException(HttpStatus.BAD_REQUEST,
                    "El prefijo 'amq.' está reservado por RabbitMQ");
        }
    }

    private void noSistema(String nombre) {
        if (nombre.startsWith(PREFIJO_SISTEMA)) {
            throw new ReglaAdminException(HttpStatus.CONFLICT,
                    "'" + nombre + "' es parte de la topología del sistema (pedidos360.*) y no se puede eliminar");
        }
    }
}