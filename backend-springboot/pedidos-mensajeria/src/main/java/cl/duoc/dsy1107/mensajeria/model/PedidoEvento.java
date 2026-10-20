package cl.duoc.dsy1107.mensajeria.model;

public record PedidoEvento(String eventoId, Long pedidoId, String clienteId, String tipo, String fecha) {
}