package cl.duoc.dsy1107.kafkaadmin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearTopicoRequest(

        @NotBlank(message = "El nombre del topico es obligatorio") @Size(max = 100, message = "El nombre no puede superar 100 caracteres") @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$", message = "El nombre debe empezar con letra o numero y solo admite letras, numeros, punto, guion y guion bajo") String nombre,

        @Min(value = 1, message = "Debe tener al menos 1 particion") @Max(value = 12, message = "No puede superar 12 particiones") Integer particiones,

        @Min(value = 1, message = "Debe tener al menos 1 replica") @Max(value = 3, message = "No puede superar 3 replicas (hay 3 brokers)") Integer replicas,

        @Min(value = 60_000, message = "La retencion minima es 60000 ms (1 minuto)") @Max(value = 2_592_000_000L, message = "La retencion maxima es 2592000000 ms (30 dias)") Long retencionMs) {
}