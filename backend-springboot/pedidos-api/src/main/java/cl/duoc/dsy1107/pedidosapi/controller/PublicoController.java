package cl.duoc.dsy1107.pedidosapi.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PublicoController {

    @GetMapping("/publico")
    public Map<String, String> publico() {
        return Map.of(
                "mensaje", "Endpoint público operativo",
                "aplicacion", "Pedidos360",
                "arquitectura", "Angular -> Entra ID -> API Gateway -> Spring Boot/EC2 -> RDS PostgreSQL");
    }
}
