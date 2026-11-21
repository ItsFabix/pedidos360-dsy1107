package cl.duoc.dsy1107.auditoria.modelo;

public record RegistroAlerta(AlertaPedido alerta, int particion, long offset) {
}