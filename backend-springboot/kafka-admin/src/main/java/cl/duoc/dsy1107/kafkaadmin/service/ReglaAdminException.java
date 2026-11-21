package cl.duoc.dsy1107.kafkaadmin.service;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una operacion incumple una regla del servicio. Lleva el
 * codigo HTTP que corresponde.
 */
public class ReglaAdminException extends RuntimeException {

    private final HttpStatus estado;

    public ReglaAdminException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}