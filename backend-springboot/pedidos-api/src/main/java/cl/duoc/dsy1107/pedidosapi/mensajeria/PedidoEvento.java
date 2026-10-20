package cl.duoc.dsy1107.pedidosapi.mensajeria;

public record PedidoEvento(String eventoId, Long pedidoId, String clienteId, String tipo, String fecha) {
}