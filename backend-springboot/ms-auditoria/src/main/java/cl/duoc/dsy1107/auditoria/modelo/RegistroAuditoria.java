package cl.duoc.dsy1107.auditoria.modelo;

/**
 * Un evento ya auditado, junto con el lugar de Kafka de donde salio.
 * Guardar particion y offset sirve para demostrar el orden por key.
 */
public record RegistroAuditoria(PedidoEvento evento, int particion, long offset) {
}