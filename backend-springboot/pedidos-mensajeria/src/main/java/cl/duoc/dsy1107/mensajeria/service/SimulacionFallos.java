package cl.duoc.dsy1107.mensajeria.service;

import java.util.Set;
import java.util.TreeSet;
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

    public static boolean esDominioValido(String dominio) {
        return DOMINIOS_VALIDOS.contains(dominio);
    }

    public void activar(String dominio) {
        activos.add(dominio);
    }

    public void desactivar(String dominio) {
        activos.remove(dominio);
    }

    public Set<String> activos() {
        return new TreeSet<>(activos);
    }

    /** Lanza una excepción si el dominio está marcado para fallar. */
    public void verificar(String dominio) {
        if (activos.contains(dominio)) {
            throw new IllegalStateException("Fallo simulado en el dominio " + dominio);
        }
    }
}