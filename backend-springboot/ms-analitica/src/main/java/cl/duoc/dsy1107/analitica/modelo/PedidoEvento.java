package cl.duoc.dsy1107.analitica.modelo;

/** Evento de pedido tal como lo publica pedidos-api. */
public record PedidoEvento(
        String eventoId,
        Long pedidoId,
        String clienteId,
        String tipo,
        String fecha) {
}