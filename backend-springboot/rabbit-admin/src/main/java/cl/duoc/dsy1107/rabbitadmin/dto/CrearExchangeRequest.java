package cl.duoc.dsy1107.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearExchangeRequest(

        @NotBlank(message = "El nombre del exchange es obligatorio") @Size(max = 100, message = "El nombre no puede superar 100 caracteres") @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "El nombre solo admite letras, números, punto, guion y guion bajo") String nombre,

        @NotNull(message = "El tipo es obligatorio: DIRECT, TOPIC o FANOUT") TipoExchange tipo,

        Boolean durable) {
}