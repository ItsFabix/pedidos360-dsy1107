package cl.duoc.dsy1107.mensajeria.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import cl.duoc.dsy1107.mensajeria.config.MensajeriaProperties;
import cl.duoc.dsy1107.mensajeria.config.RabbitNombres;

@Component
public class SimulacionFallos {

    private static final Set<String> DOMINIOS_VALIDOS = Set.of(
            RabbitNombres.DOMINIO_NOTIFICACIONES,
            RabbitNombres.DOMINIO_COCINA,
            RabbitNombres.DOMINIO_DOCUMENTOS);

    private final Set<String> activos = ConcurrentHashMap.newKeySet();

    public SimulacionFallos(MensajeriaProperties propiedades) {
        propiedades.dominiosConFallo().stream()
                .map(String::trim)
                .filter(DOMINIOS_VALIDOS::contains)
                .forEach(activos::add);
    }

    /** Lanza una excepción si el dominio está marcado para fallar. */
    public void verificar(String dominio) {
        if (activos.contains(dominio)) {
            throw new IllegalStateException("Fallo simulado en el dominio " + dominio);
        }
    }
}