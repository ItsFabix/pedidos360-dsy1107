package cl.duoc.dsy1107.rabbitadmin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearColaRequest(

        @NotBlank(message = "El nombre de la cola es obligatorio") @Size(max = 100, message = "El nombre no puede superar 100 caracteres") @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "El nombre solo admite letras, números, punto, guion y guion bajo") String nombre,

        Boolean durable,

        @Min(value = 1000, message = "El TTL mínimo es 1000 ms") @Max(value = 86_400_000, message = "El TTL máximo es 86400000 ms (24 horas)") Long ttlMs,

        @Min(value = 1, message = "maxLength debe ser al menos 1") @Max(value = 100_000, message = "maxLength no puede superar 100000") Integer maxLength,

        @Size(max = 100, message = "El nombre del exchange no puede superar 100 caracteres") @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "Nombre de exchange inválido") String deadLetterExchange,

        @Size(max = 255, message = "La routing key no puede superar 255 caracteres") @Pattern(regexp = "^[A-Za-z0-9._*#-]*$", message = "Routing key inválida") String deadLetterRoutingKey) {
}