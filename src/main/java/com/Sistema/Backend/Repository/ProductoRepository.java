package com.Sistema.Backend.Repository;

import com.Sistema.Backend.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Para mostrar solo lo que hay en existencia en el link del cliente
    List<Producto> findByDisponibleTrue();
}
