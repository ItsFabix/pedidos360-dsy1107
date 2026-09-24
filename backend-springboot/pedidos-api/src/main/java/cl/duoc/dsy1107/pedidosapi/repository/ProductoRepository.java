package cl.duoc.dsy1107.pedidosapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.dsy1107.pedidosapi.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}