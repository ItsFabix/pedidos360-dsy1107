package cl.duoc.dsy1107.rabbitadmin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.dsy1107.rabbitadmin.dto.ColaInfo;
import cl.duoc.dsy1107.rabbitadmin.dto.CrearBindingRequest;
import cl.duoc.dsy1107.rabbitadmin.dto.CrearColaRequest;
import cl.duoc.dsy1107.rabbitadmin.dto.CrearExchangeRequest;
import cl.duoc.dsy1107.rabbitadmin.service.RabbitAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rabbit")
@Tag(name = "Administración de RabbitMQ")
public class RabbitAdminController {

    private final RabbitAdminService servicio;

    public RabbitAdminController(RabbitAdminService servicio) {
        this.servicio = servicio;
    }

    // ------------------------------------------------------------------ colas

    @Operation(summary = "Crear una cola")
    @PostMapping("/queues")
    @ResponseStatus(HttpStatus.CREATED)
    public ColaInfo crearCola(@Valid @RequestBody CrearColaRequest request) {
        return servicio.crearCola(request);
    }

    @Operation(summary = "Consultar el estado de una cola")
    @GetMapping("/queues/{nombre}")
    public ColaInfo consultarCola(@PathVariable String nombre) {
        return servicio.consultarCola(nombre);
    }

    @Operation(summary = "Eliminar una cola (las de pedidos360.* están protegidas)")
    @DeleteMapping("/queues/{nombre}")
    public ResponseEntity<Void> eliminarCola(@PathVariable String nombre) {
        servicio.eliminarCola(nombre);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- exchanges

    @Operation(summary = "Crear un exchange")
    @PostMapping("/exchanges")
    @ResponseStatus(HttpStatus.CREATED)
    public void crearExchange(@Valid @RequestBody CrearExchangeRequest request) {
        servicio.crearExchange(request);
    }

    @Operation(summary = "Eliminar un exchange (los de pedidos360.* están protegidos)")
    @DeleteMapping("/exchanges/{nombre}")
    public ResponseEntity<Void> eliminarExchange(@PathVariable String nombre) {
        servicio.eliminarExchange(nombre);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------------------- bindings

    @Operation(summary = "Crear un binding entre un exchange y una cola")
    @PostMapping("/bindings")
    @ResponseStatus(HttpStatus.CREATED)
    public void crearBinding(@Valid @RequestBody CrearBindingRequest request) {
        servicio.crearBinding(request);
    }

    @Operation(summary = "Eliminar un binding")
    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(@RequestParam String cola,
            @RequestParam String exchange,
            @RequestParam(defaultValue = "") String routingKey) {
        servicio.eliminarBinding(cola, exchange, routingKey);
        return ResponseEntity.noContent().build();
    }
}