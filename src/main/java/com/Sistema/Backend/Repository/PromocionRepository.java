package com.Sistema.Backend.Repository;

import com.Sistema.Backend.Entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    // Buscar promociones activas y vigentes según el tiempo del servidor
    @Query("SELECT p FROM Promocion p WHERE p.activa = true AND (p.fechaInicio IS NULL OR p.fechaInicio <= CURRENT_TIMESTAMP) AND (p.fechaFin IS NULL OR p.fechaFin >= CURRENT_TIMESTAMP)")
    List<Promocion> findPromocionesVigentes();
}
