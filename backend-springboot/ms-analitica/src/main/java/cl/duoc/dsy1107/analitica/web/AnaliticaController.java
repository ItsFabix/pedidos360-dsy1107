package cl.duoc.dsy1107.analitica.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.dsy1107.analitica.modelo.ResumenAnalitica;
import cl.duoc.dsy1107.analitica.servicio.AnaliticaServicio;

@RestController
@RequestMapping("/api/analitica")
public class AnaliticaController {

    private final AnaliticaServicio servicio;

    public AnaliticaController(AnaliticaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/resumen")
    public ResumenAnalitica resumen() {
        return servicio.resumen();
    }
}