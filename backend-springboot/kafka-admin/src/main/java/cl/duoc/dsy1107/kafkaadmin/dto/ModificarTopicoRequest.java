package cl.duoc.dsy1107.kafkaadmin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/** Todos los campos son opcionales, pero debe venir al menos uno. */
public record ModificarTopicoRequest(

        @Min(value = 60_000, message = "La retencion minima es 60000 ms (1 minuto)") @Max(value = 2_592_000_000L, message = "La retencion maxima es 2592000000 ms (30 dias)") Long retencionMs,

        @Pattern(regexp = "^(delete|compact)$", message = "cleanupPolicy debe ser delete o compact") String cleanupPolicy,

        @Min(value = 1, message = "Debe tener al menos 1 particion") @Max(value = 12, message = "No puede superar 12 particiones") Integer particiones) {
}