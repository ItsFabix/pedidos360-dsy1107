package cl.duoc.dsy1107.pedidosapi.mensajeria;

import cl.duoc.dsy1107.pedidosapi.model.EstadoPedido;

public record PedidoEstadoCambiado(Long pedidoId, String clienteId, EstadoPedido estado) {
}