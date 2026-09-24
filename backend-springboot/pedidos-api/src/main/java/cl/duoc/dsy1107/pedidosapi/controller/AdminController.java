package cl.duoc.dsy1107.pedidosapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.dsy1107.pedidosapi.service.PedidoService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PedidoService service;

    public AdminController(PedidoService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumen(@AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");

        return Map.of(
            "mensaje", "Acceso Admin autorizado",
            "usuario", jwt.getSubject(),
            "roles", roles == null ? List.of() : roles,
            "cantidadPedidos", service.cantidad()
        );
    }
}
