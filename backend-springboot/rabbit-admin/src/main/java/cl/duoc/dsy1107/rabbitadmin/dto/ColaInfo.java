package cl.duoc.dsy1107.rabbitadmin.dto;

/**
 * Estado básico de una cola: cuántos mensajes tiene y cuántos consumidores la
 * atienden.
 */
public record ColaInfo(String nombre, int mensajes, int consumidores) {
}