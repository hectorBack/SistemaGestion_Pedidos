package com.Sistema.Backend.Proveedores.Repository;

import com.Sistema.Backend.Categorias.Entity.ProveedorInsumo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorInsumoRepository extends JpaRepository<ProveedorInsumo, Long> {

    // Buscar el catálogo de insumos de un proveedor específico de forma paginada
    @Query("SELECT pi FROM ProveedorInsumo pi WHERE pi.proveedor.id = :proveedorId AND (:activo IS NULL OR pi.activo = :activo)")
    Page<ProveedorInsumo> findByProveedorIdAndActivo(
            @Param("proveedorId") Long proveedorId,
            @Param("activo") Boolean activo,
            Pageable pageable);

    // Buscar qué proveedores venden un insumo específico (Filtrando por activos)
    List<ProveedorInsumo> findByInsumoIdAndActivoTrue(Long insumoId);

    // Evitar que se asigne dos veces el mismo insumo al mismo proveedor
    boolean existsByProveedorIdAndInsumoId(Long proveedorId, Long insumoId);

    // Evitar duplicados en actualización
    boolean existsByProveedorIdAndInsumoIdAndIdNot(Long proveedorId, Long insumoId, Long id);

    // Buscar una relación específica ignorando el Soft Delete (útil para reactivar registros)
    Optional<ProveedorInsumo> findByProveedorIdAndInsumoId(Long proveedorId, Long insumoId);
}
