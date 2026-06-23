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

    // 🌟 NUEVA QUERY AVANZADA CON MÚLTIPLES FILTROS OPCIONALES
    @Query("SELECT p FROM Promocion p WHERE " +
            "(CAST(:nombre AS string) IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS string), '%'))) AND " +
            "(:activa IS NULL OR p.activa = :activa)")
    Page<Promocion> buscarConFiltrosPaginados(
            @Param("nombre") String nombre,
            @Param("activa") Boolean activa,
            Pageable pageable
    );

    // Conteo Total (Todas las que existen en la tabla)
    @Query("SELECT COUNT(p.id) FROM Promocion p")
    long countTotal();

    // Conteo Activas (Switch encendido y dentro del rango de fechas)
    @Query("SELECT COUNT(p.id) FROM Promocion p WHERE p.activa = true AND (p.fechaFin IS NULL OR :ahora <= p.fechaFin) AND (p.fechaInicio IS NULL OR :ahora >= p.fechaInicio)")
    long countActivas(@Param("ahora") LocalDateTime ahora);

    // Conteo Programadas (Switch encendido pero fecha de inicio a futuro)
    @Query("SELECT COUNT(p.id) FROM Promocion p WHERE p.fechaInicio IS NOT NULL AND p.fechaInicio > :ahora")
    long countProgramadas(@Param("ahora") LocalDateTime ahora);

    // Conteo Expiradas (Fecha de fin ya pasó, sin importar el switch)
    @Query("SELECT COUNT(p.id) FROM Promocion p WHERE p.fechaFin IS NOT NULL AND :ahora > p.fechaFin")
    long countExpiradas(@Param("ahora") LocalDateTime ahora);
}
