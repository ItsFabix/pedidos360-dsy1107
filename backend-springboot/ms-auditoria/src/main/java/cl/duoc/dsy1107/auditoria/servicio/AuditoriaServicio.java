package cl.duoc.dsy1107.auditoria.servicio;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.dsy1107.auditoria.modelo.RegistroAuditoria;

/**
 * Guarda la linea de tiempo de cada pedido. Logica de negocio pura: no conoce
 * Kafka.
 *
 * Kafka entrega "al menos una vez", asi que un evento puede llegar repetido.
 * Por eso se descarta el que tenga un eventoId ya registrado (idempotencia).
 *
 * Limite conocido: el almacenamiento es en memoria. Al reiniciar el servicio se
 * pierde la linea de tiempo, y el grupo continua desde el ultimo offset
 * confirmado
 * (no relee todo). Para reconstruirla hay que reiniciar los offsets del grupo
 * (kafka-consumer-groups --reset-offsets --to-earliest) o guardar en una base
 * de datos.
 */
@Service
public class AuditoriaServicio {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaServicio.class);

    private final Map<Long, List<RegistroAuditoria>> lineasDeTiempo = new ConcurrentHashMap<>();

    /** @return true si el evento era nuevo, false si ya estaba registrado. */
    public boolean registrar(RegistroAuditoria registro) {
        boolean[] nuevo = { false };
        lineasDeTiempo.compute(registro.evento().pedidoId(), (pedidoId, lista) -> {
            List<RegistroAuditoria> actual = (lista == null) ? new CopyOnWriteArrayList<>() : lista;
            boolean repetido = actual.stream()
                    .anyMatch(r -> r.evento().eventoId().equals(registro.evento().eventoId()));
            if (!repetido) {
                actual.add(registro);
                nuevo[0] = true;
            }
            return actual;
        });

        if (nuevo[0]) {
            log.info("Auditado pedido={} tipo={} particion={} offset={}",
                    registro.evento().pedidoId(), registro.evento().tipo(),
                    registro.particion(), registro.offset());
        } else {
            log.warn("Evento duplicado descartado: eventoId={}", registro.evento().eventoId());
        }
        return nuevo[0];
    }

    public List<RegistroAuditoria> consultar(Long pedidoId) {
        List<RegistroAuditoria> lista = lineasDeTiempo.get(pedidoId);
        return lista == null ? List.of() : List.copyOf(lista);
    }
}