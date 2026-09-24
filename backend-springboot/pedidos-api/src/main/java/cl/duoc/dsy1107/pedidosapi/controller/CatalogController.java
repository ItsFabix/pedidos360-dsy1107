package cl.duoc.dsy1107.pedidosapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.dsy1107.pedidosapi.dto.ProductoRequest;
import cl.duoc.dsy1107.pedidosapi.model.Producto;
import cl.duoc.dsy1107.pedidosapi.service.ProductoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalog/products")
public class CatalogController {

    private final ProductoService service;

    public CatalogController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Producto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Producto buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@Valid @RequestBody ProductoRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        return service.actualizar(id, request);
    }
}