package cl.duoc.dsy1107.pedidosapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.dsy1107.pedidosapi.dto.CambiarEstadoRequest;
import cl.duoc.dsy1107.pedidosapi.dto.CrearPedidoRequest;
import cl.duoc.dsy1107.pedidosapi.model.Pedido;
import cl.duoc.dsy1107.pedidosapi.service.PedidoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Pedido> listar(@AuthenticationPrincipal Jwt jwt) {
        if (esClienteSolamente(jwt)) {
            return service.listarDelCliente(jwt.getSubject());
        }
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public Pedido detalle(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Pedido pedido = service.buscarPorId(id);

        if (esClienteSolamente(jwt) && !pedido.getClienteId().equals(jwt.getSubject())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No puede consultar pedidos de otro cliente");
        }

        return pedido;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido crear(@Valid @RequestBody CrearPedidoRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String nombre = jwt.getClaimAsString("name");
        return service.crear(request, jwt.getSubject(), nombre);
    }

    @PutMapping("/{id}/status")
    public Pedido cambiarEstado(@PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {
        return service.cambiarEstado(id, request.getEstado());
    }

    private boolean esClienteSolamente(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            return true;
        }
        return !roles.contains("Admin") && !roles.contains("Operador");
    }
}