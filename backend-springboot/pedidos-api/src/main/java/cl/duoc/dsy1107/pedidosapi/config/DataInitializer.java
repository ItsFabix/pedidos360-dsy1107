package cl.duoc.dsy1107.pedidosapi.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.duoc.dsy1107.pedidosapi.model.Producto;
import cl.duoc.dsy1107.pedidosapi.repository.ProductoRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarDatosIniciales(ProductoRepository productoRepository) {
        return args -> {
            if (productoRepository.count() == 0) {
                productoRepository.save(new Producto(
                        "Notebook Lenovo IdeaPad", "14 pulgadas, 16GB RAM",
                        new BigDecimal("699990.00"), 10));
                productoRepository.save(new Producto(
                        "Mouse Logitech M170", "Inalámbrico",
                        new BigDecimal("19990.00"), 50));
                productoRepository.save(new Producto(
                        "Teclado mecánico Redragon", "Switch rojo, español",
                        new BigDecimal("29990.00"), 25));
                productoRepository.save(new Producto(
                        "Monitor Samsung 24\"", "Full HD, 75Hz",
                        new BigDecimal("129990.00"), 8));
            }
        };
    }
}