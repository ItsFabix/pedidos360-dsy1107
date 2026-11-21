package cl.duoc.dsy1107.auditoria.modelo;

/** Alerta publicada por ms-analitica en el topico de alertas. */
public record AlertaPedido(
        String alertaId,
        Long pedidoId,
        String tipo,
        String detalle,
        String fecha) {
}