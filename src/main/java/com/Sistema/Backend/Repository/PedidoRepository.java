package com.Sistema.Backend.Repository;

import com.Sistema.Backend.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Entity.EstadoPedido;
import com.Sistema.Backend.Entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Para el monitor de cocina: ver pedidos pendientes o en preparación
    // Ordenados por fecha para atender al que llegó primero (FIFO)
    List<Pedido> findByEstadoInOrderByFechaCreacionAsc(List<EstadoPedido> estados);

    // Para buscar rápidamente por los últimos dígitos de WhatsApp
    List<Pedido> findByWhatsappFinal(String whatsappFinal);

    // Consulta para el Reporte: Contar pedidos en un rango de fechas
    long countByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    //Consulta para el Reporte: Sumar ingresos en un rango de fechas
    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.fechaCreacion BETWEEN :inicio AND :fin AND p.estado != 'CANCELADO'")
    BigDecimal sumarTotalVentasPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Filtro avanzado: Por estado (opcional) y rango de fechas
    @Query("SELECT p FROM Pedido p WHERE " +
            "(:estado IS NULL OR p.estado = :estado) AND " +
            "(p.fechaCreacion BETWEEN :inicio AND :fin)" +
            "ORDER BY p.fechaCreacion DESC")
    Page<Pedido> filtrarPedidos(
            @Param("estado") EstadoPedido estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            Pageable pageable);
}
