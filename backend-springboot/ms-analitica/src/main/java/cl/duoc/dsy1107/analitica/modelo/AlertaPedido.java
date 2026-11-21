package cl.duoc.dsy1107.analitica.modelo;

/** Alerta que ms-analitica publica en el topico de alertas. */
public record AlertaPedido(
        String alertaId,
        Long pedidoId,
        String tipo,
        String detalle,
        String fecha) {
}