package cl.duoc.dsy1107.rabbitadmin.error;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.duoc.dsy1107.rabbitadmin.dto.ErrorRespuesta;
import cl.duoc.dsy1107.rabbitadmin.service.ReglaAdminException;

/** Traduce las excepciones a respuestas JSON con el código HTTP correcto. */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ReglaAdminException.class)
    public ResponseEntity<ErrorRespuesta> reglaIncumplida(ReglaAdminException e) {
        return ResponseEntity.status(e.getEstado())
                .body(new ErrorRespuesta(e.getEstado().value(), e.getMessage(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(MethodArgumentNotValidException e) {
        Map<String, String> detalles = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> detalles.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ErrorRespuesta(400, "Los datos enviados no son válidos", detalles));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> cuerpoIlegible(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorRespuesta(400, "El cuerpo de la petición no es un JSON válido", Map.of()));
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<ErrorRespuesta> brokerNoDisponible(AmqpException e) {
        log.error("Falla al comunicarse con RabbitMQ", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorRespuesta(503, "No se pudo completar la operación en RabbitMQ", Map.of()));
    }
}