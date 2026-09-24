package cl.duoc.dsy1107.pedidosapi.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.dsy1107.pedidosapi.dto.CrearPedidoRequest;
import cl.duoc.dsy1107.pedidosapi.model.EstadoPedido;
import cl.duoc.dsy1107.pedidosapi.model.ItemPedido;
import cl.duoc.dsy1107.pedidosapi.model.Pedido;
import cl.duoc.dsy1107.pedidosapi.model.Producto;
import cl.duoc.dsy1107.pedidosapi.repository.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final ProductoService productoService;

    public PedidoService(PedidoRepository repository, ProductoService productoService) {
        this.repository = repository;
        this.productoService = productoService;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return repository.findAllByOrderByFechaCreacionDesc();
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarDelCliente(String clienteId) {
        return repository.findByClienteIdOrderByFechaCreacionDesc(clienteId);
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return repository.findWithItemsById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido no encontrado: " + id));
    }

    @Transactional
    public Pedido crear(CrearPedidoRequest request, String clienteId, String clienteNombre) {
        Pedido pedido = new Pedido(clienteId, clienteNombre, request.getDescripcion());

        for (CrearPedidoRequest.ItemRequest itemRequest : request.getItems()) {
            Producto producto = productoService.buscarPorId(itemRequest.getProductoId());

            pedido.agregarItem(new ItemPedido(
                    producto.getId(),
                    producto.getNombre(),
                    itemRequest.getCantidad(),
                    producto.getPrecio()));
        }

        return repository.save(pedido);
    }

    @Transactional
    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = buscarPorId(id);
        EstadoPedido actual = pedido.getEstado();

        if (actual == nuevoEstado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido ya se encuentra en estado " + actual);
        }

        if (actual.esFinal()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido está en estado final " + actual + " y no admite cambios");
        }

        if (!actual.puedeAvanzarA(nuevoEstado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transición no permitida: " + actual + " → " + nuevoEstado
                            + ". Estados posibles: " + actual.siguientesPosibles());
        }

        // Regla de negocio: al ACEPTAR el pedido se descuenta el stock
        // Regla de negocio: al ACEPTAR el pedido se descuenta el stock
        if (nuevoEstado == EstadoPedido.ACEPTADO) {
            for (ItemPedido item : pedido.getItems()) {
                productoService.descontarStock(item.getProductoId(), item.getCantidad());
            }
        }

        // Si se cancela un pedido que ya había descontado stock, se repone
        if (nuevoEstado == EstadoPedido.CANCELADO && actual != EstadoPedido.CREADO) {
            for (ItemPedido item : pedido.getItems()) {
                productoService.reponerStock(item.getProductoId(), item.getCantidad());
            }
        }

        pedido.setEstado(nuevoEstado);
        return repository.save(pedido);
    }

    @Transactional(readOnly = true)
    public long cantidad() {
        return repository.count();
    }
}