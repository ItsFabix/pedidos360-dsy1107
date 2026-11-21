package cl.duoc.dsy1107.auditoria.modelo;

/**
 * Evento de pedido tal como lo publica pedidos-api en Kafka.
 * Los nombres de los campos deben coincidir con el JSON del topico.
 */
public record PedidoEvento(
        String eventoId,
        Long pedidoId,
        String clienteId,
        String tipo,
        String fecha) {
}