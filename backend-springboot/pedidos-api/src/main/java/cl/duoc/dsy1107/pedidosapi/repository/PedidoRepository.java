package cl.duoc.dsy1107.pedidosapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.dsy1107.pedidosapi.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = "items")
    List<Pedido> findAllByOrderByFechaCreacionDesc();

    @EntityGraph(attributePaths = "items")
    List<Pedido> findByClienteIdOrderByFechaCreacionDesc(String clienteId);

    @EntityGraph(attributePaths = "items")
    Optional<Pedido> findWithItemsById(Long id);
}