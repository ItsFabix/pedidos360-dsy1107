package cl.duoc.dsy1107.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearBindingRequest(

        @NotBlank(message = "La cola es obligatoria") @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Nombre de cola inválido") @Size(max = 100) String cola,

        @NotBlank(message = "El exchange es obligatorio") @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Nombre de exchange inválido") @Size(max = 100) String exchange,

        @Size(max = 255, message = "La routing key no puede superar 255 caracteres") @Pattern(regexp = "^[A-Za-z0-9._*#-]*$", message = "Routing key inválida") String routingKey) {
}