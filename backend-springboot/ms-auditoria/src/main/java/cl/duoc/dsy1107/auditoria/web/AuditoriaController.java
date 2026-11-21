package cl.duoc.dsy1107.auditoria.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.dsy1107.auditoria.modelo.RegistroAlerta;
import cl.duoc.dsy1107.auditoria.modelo.RegistroAuditoria;
import cl.duoc.dsy1107.auditoria.servicio.AuditoriaServicio;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaServicio servicio;

    public AuditoriaController(AuditoriaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/pedidos/{id}")
    public List<RegistroAuditoria> lineaDeTiempo(@PathVariable Long id) {
        List<RegistroAuditoria> lista = servicio.consultar(id);
        if (lista.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Sin eventos auditados para el pedido " + id);
        }
        return lista;
    }

    @GetMapping("/alertas")
    public List<RegistroAlerta> alertas() {
        return servicio.consultarAlertas();
    }
}