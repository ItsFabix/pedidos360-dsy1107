package cl.duoc.dsy1107.rabbitadmin.service;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una operación incumple una regla del servicio. Lleva el
 * código HTTP que corresponde.
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