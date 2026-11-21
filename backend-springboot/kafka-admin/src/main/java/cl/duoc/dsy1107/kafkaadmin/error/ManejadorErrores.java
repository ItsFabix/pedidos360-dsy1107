package cl.duoc.dsy1107.kafkaadmin.error;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.duoc.dsy1107.kafkaadmin.dto.ErrorRespuesta;
import cl.duoc.dsy1107.kafkaadmin.service.ReglaAdminException;

/** Traduce las excepciones a respuestas JSON con el codigo HTTP correcto. */
@RestControllerAdvice
public class ManejadorErrores {

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
                .body(new ErrorRespuesta(400, "Los datos enviados no son validos", detalles));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> cuerpoIlegible(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorRespuesta(400, "El cuerpo de la peticion no es un JSON valido", Map.of()));
    }
}