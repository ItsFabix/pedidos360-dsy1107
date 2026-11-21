package cl.duoc.dsy1107.kafkaadmin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.dsy1107.kafkaadmin.dto.CrearTopicoRequest;
import cl.duoc.dsy1107.kafkaadmin.dto.GrupoDetalle;
import cl.duoc.dsy1107.kafkaadmin.dto.GrupoResumen;
import cl.duoc.dsy1107.kafkaadmin.dto.ModificarTopicoRequest;
import cl.duoc.dsy1107.kafkaadmin.dto.TopicoDetalle;
import cl.duoc.dsy1107.kafkaadmin.dto.TopicoResumen;
import cl.duoc.dsy1107.kafkaadmin.service.KafkaAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Endpoints REST de administracion de Kafka. Solo valida la entrada y delega en
 * el servicio.
 */
@RestController
@RequestMapping("/api/kafka")
@Tag(name = "Administracion de Kafka")
public class KafkaAdminController {

    private final KafkaAdminService servicio;

    public KafkaAdminController(KafkaAdminService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Lista los topicos con su numero de particiones y replicas")
    @GetMapping("/topicos")
    public List<TopicoResumen> listarTopicos() {
        return servicio.listarTopicos();
    }

    @Operation(summary = "Detalle de un topico: configuracion y lider/replicas/ISR por particion")
    @GetMapping("/topicos/{nombre}")
    public TopicoDetalle describirTopico(@PathVariable String nombre) {
        return servicio.describirTopico(nombre);
    }

    @Operation(summary = "Crea un topico (por defecto 3 particiones, 3 replicas, retencion 7 dias)")
    @PostMapping("/topicos")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicoResumen crearTopico(@Valid @RequestBody CrearTopicoRequest solicitud) {
        return servicio.crearTopico(solicitud);
    }

    @Operation(summary = "Modifica retencion, politica de limpieza o aumenta particiones")
    @PatchMapping("/topicos/{nombre}")
    public TopicoDetalle modificarTopico(@PathVariable String nombre,
            @Valid @RequestBody ModificarTopicoRequest solicitud) {
        return servicio.modificarTopico(nombre, solicitud);
    }

    @Operation(summary = "Elimina un topico (los pedidos360.* estan protegidos)")
    @DeleteMapping("/topicos/{nombre}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarTopico(@PathVariable String nombre) {
        servicio.eliminarTopico(nombre);
    }

    @Operation(summary = "Lista los grupos de consumo con su estado y numero de miembros")
    @GetMapping("/grupos")
    public List<GrupoResumen> listarGrupos() {
        return servicio.listarGrupos();
    }

    @Operation(summary = "Detalle de un grupo: offset actual, offset final y lag por particion")
    @GetMapping("/grupos/{grupoId}")
    public GrupoDetalle describirGrupo(@PathVariable String grupoId) {
        return servicio.describirGrupo(grupoId);
    }
}