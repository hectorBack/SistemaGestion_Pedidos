package com.Sistema.Backend.Pagos.Repository;

import com.Sistema.Backend.Pagos.Entity.MetodoPago;
import com.Sistema.Backend.Pagos.Entity.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Buscar por el código público (PAG-XXXXX)
    Optional<Pago> findByCodigoTransaccion(String codigoTransaccion);

    // Buscar el pago usando el ID del pedido relacionado
    Optional<Pago> findByPedidoId(Long pedidoId);

    // Filtro avanzado con paginación incorporada: por método y rango de fechas (con horas)
    @Query("SELECT p FROM Pago p WHERE " +
            "(:metodo IS NULL OR p.metodoPago = :metodo) AND " +
            "(p.fechaPago BETWEEN :inicio AND :fin)")
    Page<Pago> filtrarPagos(
            @Param("metodo") MetodoPago metodo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            Pageable pageable);
}
