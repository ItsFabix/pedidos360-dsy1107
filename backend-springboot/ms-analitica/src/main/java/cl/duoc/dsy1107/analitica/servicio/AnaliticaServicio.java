package cl.duoc.dsy1107.analitica.servicio;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.dsy1107.analitica.modelo.AlertaPedido;
import cl.duoc.dsy1107.analitica.modelo.PedidoEvento;
import cl.duoc.dsy1107.analitica.modelo.ResumenAnalitica;

/**
 * Logica de negocio de analitica. No conoce Kafka.
 *
 * - Cuenta eventos por estado.
 * - Detecta una anomalia: un pedido cancelado DESPUES de haber entrado a
 * preparacion o despacho (CANCELACION_TARDIA).
 * - Descarta eventos repetidos (Kafka entrega "al menos una vez").
 *
 * Limites conocidos: todo vive en memoria; y la alerta se publica de forma
 * asincrona, asi que si Kafka falla justo ahi, la alerta se pierde (queda en el
 * log).
 */
@Service
public class AnaliticaServicio {

    private static final Logger log = LoggerFactory.getLogger(AnaliticaServicio.class);

    private static final String CANCELADO = "PEDIDO_CANCELADO";
    private static final String EN_PREPARACION = "PEDIDO_EN_PREPARACION";
    private static final String DESPACHADO = "PEDIDO_DESPACHADO";

    private final Set<String> eventosVistos = ConcurrentHashMap.newKeySet();
    private final Map<String, LongAdder> porEstado = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> estadosPorPedido = new ConcurrentHashMap<>();
    private final LongAdder total = new LongAdder();
    private final LongAdder alertas = new LongAdder();

    /** @return la alerta generada, si el evento revela una anomalia. */
    public Optional<AlertaPedido> procesar(PedidoEvento evento) {
        // Validar ANTES de tocar el estado: asi un reintento no queda descartado como
        // "duplicado".
        Objects.requireNonNull(evento.eventoId(), "eventoId requerido");
        Objects.requireNonNull(evento.pedidoId(), "pedidoId requerido");
        Objects.requireNonNull(evento.tipo(), "tipo requerido");

        if (!eventosVistos.add(evento.eventoId())) {
            log.warn("Evento duplicado descartado: eventoId={}", evento.eventoId());
            return Optional.empty();
        }

        total.increment();
        porEstado.computeIfAbsent(evento.tipo(), t -> new LongAdder()).increment();

        Set<String> estados = estadosPorPedido.computeIfAbsent(
                evento.pedidoId(), id -> ConcurrentHashMap.newKeySet());
        boolean cancelacionTardia = CANCELADO.equals(evento.tipo())
                && (estados.contains(EN_PREPARACION) || estados.contains(DESPACHADO));
        estados.add(evento.tipo());

        log.info("Analizado pedido={} tipo={}", evento.pedidoId(), evento.tipo());

        if (!cancelacionTardia) {
            return Optional.empty();
        }
        alertas.increment();
        return Optional.of(new AlertaPedido(
                UUID.randomUUID().toString(),
                evento.pedidoId(),
                "CANCELACION_TARDIA",
                "Pedido cancelado despues de entrar a preparacion o despacho",
                OffsetDateTime.now().toString()));
    }

    public ResumenAnalitica resumen() {
        Map<String, Long> conteo = porEstado.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, e -> e.getValue().sum(), (a, b) -> a, TreeMap::new));
        return new ResumenAnalitica(total.sum(), alertas.sum(), conteo);
    }
}