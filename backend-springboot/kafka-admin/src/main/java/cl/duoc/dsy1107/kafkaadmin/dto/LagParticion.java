package cl.duoc.dsy1107.kafkaadmin.dto;

/**
 * lag = offsetFinal - offsetActual: cuantos mensajes le faltan por leer al
 * grupo en esa particion.
 */
public record LagParticion(String topico, int particion, long offsetActual, long offsetFinal, long lag) {
}