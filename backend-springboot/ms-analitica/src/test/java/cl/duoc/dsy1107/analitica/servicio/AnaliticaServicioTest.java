package cl.duoc.dsy1107.analitica.servicio;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import cl.duoc.dsy1107.analitica.modelo.AlertaPedido;
import cl.duoc.dsy1107.analitica.modelo.PedidoEvento;

class AnaliticaServicioTest {

    private final AnaliticaServicio servicio = new AnaliticaServicio();

    private PedidoEvento evento(String eventoId, long pedidoId, String tipo) {
        return new PedidoEvento(eventoId, pedidoId, "cliente-1", tipo, "2026-11-21T10:00:00Z");
    }

    @Test
    void cuentaLosEventosPorEstado() {
        servicio.procesar(evento("e1", 1, "PEDIDO_CREADO"));
        servicio.procesar(evento("e2", 2, "PEDIDO_CREADO"));
        servicio.procesar(evento("e3", 1, "PEDIDO_EN_PREPARACION"));

        var resumen = servicio.resumen();

        assertThat(resumen.totalEventos()).isEqualTo(3);
        assertThat(resumen.eventosPorEstado())
                .containsEntry("PEDIDO_CREADO", 2L)
                .containsEntry("PEDIDO_EN_PREPARACION", 1L);
    }

    @Test
    void descartaUnEventoDuplicado() {
        servicio.procesar(evento("e1", 1, "PEDIDO_CREADO"));
        servicio.procesar(evento("e1", 1, "PEDIDO_CREADO"));

        assertThat(servicio.resumen().totalEventos()).isEqualTo(1);
    }

    @Test
    void cancelarDespuesDeEntrarAPreparacionGeneraAlerta() {
        servicio.procesar(evento("e1", 7, "PEDIDO_EN_PREPARACION"));

        Optional<AlertaPedido> alerta = servicio.procesar(evento("e2", 7, "PEDIDO_CANCELADO"));

        assertThat(alerta).isPresent();
        assertThat(alerta.get().tipo()).isEqualTo("CANCELACION_TARDIA");
        assertThat(alerta.get().pedidoId()).isEqualTo(7L);
        assertThat(servicio.resumen().alertasEmitidas()).isEqualTo(1);
    }

    @Test
    void cancelarAntesDePrepararNoGeneraAlerta() {
        servicio.procesar(evento("e1", 8, "PEDIDO_CREADO"));

        Optional<AlertaPedido> alerta = servicio.procesar(evento("e2", 8, "PEDIDO_CANCELADO"));

        assertThat(alerta).isEmpty();
    }

    @Test
    void unEventoInvalidoNoQuedaMarcadoComoVisto() {
        assertThatThrownBy(() -> servicio.procesar(evento("e1", 9, null)))
                .isInstanceOf(NullPointerException.class);

        // Con el mismo id pero ya valido, se procesa: el fallo anterior no lo marco
        // como duplicado.
        servicio.procesar(evento("e1", 9, "PEDIDO_CREADO"));

        assertThat(servicio.resumen().totalEventos()).isEqualTo(1);
    }
}