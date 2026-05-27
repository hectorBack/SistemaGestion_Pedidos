package com.Sistema.Backend.Productos.Repository;

import com.Sistema.Backend.Productos.Entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Para mostrar solo lo que hay en existencia en el link del cliente
    List<Producto> findByDisponibleTrue();

    // Si no necesitas páginas, puedes conservar este (opcional)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // 🌟 REEMPLAZO AVANZADO: Para la tabla del Administrador con múltiples filtros opcionales
    @Query("SELECT p FROM Producto p WHERE " +
            "(CAST(:nombre AS string) IS NULL OR p.nombre ILIKE CONCAT('%', CAST(:nombre AS string), '%')) AND " +
            "(:categoria IS NULL OR p.categoria = :categoria) AND " +
            "(:disponible IS NULL OR p.disponible = :disponible)")
    Page<Producto> buscarConFiltrosPaginados(
            @Param("nombre") String nombre,
            @Param("categoria") String categoria,
            @Param("disponible") Boolean disponible,
            Pageable pageable
    );
}
