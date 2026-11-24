package cl.duoc.dsy1107.auditoria.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import cl.duoc.dsy1107.auditoria.modelo.AlertaPedido;
import cl.duoc.dsy1107.auditoria.modelo.PedidoEvento;
import cl.duoc.dsy1107.auditoria.modelo.RegistroAlerta;
import cl.duoc.dsy1107.auditoria.modelo.RegistroAuditoria;

class AuditoriaServicioTest {

    private final AuditoriaServicio servicio = new AuditoriaServicio();

    private RegistroAuditoria registro(String eventoId, long pedidoId, String tipo, long offset) {
        PedidoEvento evento = new PedidoEvento(eventoId, pedidoId, "cliente-1", tipo, "2026-11-21T10:00:00Z");
        return new RegistroAuditoria(evento, 2, offset);
    }

    @Test
    void registraUnEventoNuevo() {
        boolean nuevo = servicio.registrar(registro("e1", 50, "PEDIDO_CREADO", 1));

        assertThat(nuevo).isTrue();
        assertThat(servicio.consultar(50L)).hasSize(1);
    }

    @Test
    void descartaUnEventoRepetido() {
        servicio.registrar(registro("e1", 50, "PEDIDO_CREADO", 1));

        boolean nuevo = servicio.registrar(registro("e1", 50, "PEDIDO_CREADO", 1));

        assertThat(nuevo).isFalse();
        assertThat(servicio.consultar(50L)).hasSize(1);
    }

    @Test
    void mantieneElOrdenDeLlegadaDentroDeUnPedido() {
        servicio.registrar(registro("e1", 50, "PEDIDO_CREADO", 1));
        servicio.registrar(registro("e2", 50, "PEDIDO_EN_PREPARACION", 2));
        servicio.registrar(registro("e3", 50, "PEDIDO_DESPACHADO", 3));

        assertThat(servicio.consultar(50L))
                .extracting(r -> r.evento().tipo())
                .containsExactly("PEDIDO_CREADO", "PEDIDO_EN_PREPARACION", "PEDIDO_DESPACHADO");
    }

    @Test
    void unPedidoSinEventosDevuelveListaVacia() {
        assertThat(servicio.consultar(999L)).isEmpty();
    }

    @Test
    void registraAlertasYDescartaLasRepetidas() {
        AlertaPedido alerta = new AlertaPedido("a1", 7L, "CANCELACION_TARDIA", "detalle", "2026-11-21T10:00:00Z");

        boolean primera = servicio.registrarAlerta(new RegistroAlerta(alerta, 1, 0));
        boolean repetida = servicio.registrarAlerta(new RegistroAlerta(alerta, 1, 0));

        assertThat(primera).isTrue();
        assertThat(repetida).isFalse();
        assertThat(servicio.consultarAlertas()).hasSize(1);
    }
}