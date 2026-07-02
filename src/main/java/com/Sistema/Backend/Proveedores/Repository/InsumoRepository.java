package com.Sistema.Backend.Proveedores.Repository;

import com.Sistema.Backend.Proveedores.Entity.Insumo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    // 🌟 Busca insumos por nombre (ignorando mayúsculas/minúsculas) y por estado activo (Paginado)
    Page<Insumo> findByNombreContainingIgnoreCaseAndActivo(String nombre, Boolean activo, Pageable pageable);

    // 🌟 Busca todos los insumos que coincidan con el estado activo (Paginado)
    Page<Insumo> findByActivo(Boolean activo, Pageable pageable);
}
