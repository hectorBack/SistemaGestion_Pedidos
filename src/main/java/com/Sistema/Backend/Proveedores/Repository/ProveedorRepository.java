package com.Sistema.Backend.Proveedores.Repository;

import com.Sistema.Backend.Proveedores.Entity.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    // Consulta paginada avanzada para el buscador en tiempo real del Frontend
    @Query("SELECT p FROM Proveedor p WHERE " +
            "(CAST(:nombre AS string) IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS string), '%'))) AND " +
            "(:activo IS NULL OR p.activo = :activo)")
    Page<Proveedor> listarPaginado(
            @Param("nombre") String nombre,
            @Param("activo") Boolean activo,
            Pageable pageable);

    // Para selectores/dropdowns planos que solo requieran proveedores activos
    List<Proveedor> findByActivoTrue();

    // Validación de duplicados antes de guardar
    boolean existsByNombreIgnoreCase(String nombre);

    // Validación de duplicados al actualizar (ignora el registro actual)
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}