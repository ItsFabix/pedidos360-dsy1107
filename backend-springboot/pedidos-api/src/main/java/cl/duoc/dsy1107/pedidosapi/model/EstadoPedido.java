package cl.duoc.dsy1107.pedidosapi.model;

import java.util.List;
import java.util.Map;

public enum EstadoPedido {

    CREADO,
    ACEPTADO,
    EN_PREPARACION,
    DESPACHADO,
    ENTREGADO,
    CANCELADO;

    private static final Map<EstadoPedido, List<EstadoPedido>> TRANSICIONES = Map.of(
            CREADO, List.of(ACEPTADO, CANCELADO),
            ACEPTADO, List.of(EN_PREPARACION, CANCELADO),
            EN_PREPARACION, List.of(DESPACHADO, CANCELADO),
            DESPACHADO, List.of(ENTREGADO),
            ENTREGADO, List.of(),
            CANCELADO, List.of());

    public boolean puedeAvanzarA(EstadoPedido destino) {
        return TRANSICIONES.get(this).contains(destino);
    }

    public List<EstadoPedido> siguientesPosibles() {
        return TRANSICIONES.get(this);
    }

    public boolean esFinal() {
        return TRANSICIONES.get(this).isEmpty();
    }
}