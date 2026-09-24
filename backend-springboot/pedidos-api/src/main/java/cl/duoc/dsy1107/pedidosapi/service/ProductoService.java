package cl.duoc.dsy1107.pedidosapi.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.dsy1107.pedidosapi.dto.ProductoRequest;
import cl.duoc.dsy1107.pedidosapi.model.Producto;
import cl.duoc.dsy1107.pedidosapi.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));
    }

    @Transactional
    public Producto crear(ProductoRequest request) {
        Producto producto = new Producto(
                request.getNombre(),
                request.getDescripcion(),
                request.getPrecio(),
                request.getStock());
        return repository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarPorId(id);
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        return repository.save(producto);
    }

    @Transactional
    public void descontarStock(Long productoId, int cantidad) {
        Producto producto = buscarPorId(productoId);

        if (producto.getStock() < cantidad) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock insuficiente para el producto " + producto.getNombre()
                            + ". Disponible: " + producto.getStock() + ", solicitado: " + cantidad);
        }

        producto.setStock(producto.getStock() - cantidad);
        repository.save(producto);
    }
}