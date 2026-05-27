package com.Sistema.Backend.Promociones.Repository;

import com.Sistema.Backend.Promociones.Entity.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    // Cambiamos CURRENT_TIMESTAMP por el parámetro :ahora enviado desde Java
    @Query("SELECT p FROM Promocion p WHERE p.activa = true AND " +
            "(p.fechaInicio IS NULL OR p.fechaInicio <= :ahora) AND " +
            "(p.fechaFin IS NULL OR p.fechaFin >= :ahora)")
    List<Promocion> findPromocionesVigentes(@Param("ahora") LocalDateTime ahora);

    // Para la tabla del Administrador con paginación
    Page<Promocion> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
