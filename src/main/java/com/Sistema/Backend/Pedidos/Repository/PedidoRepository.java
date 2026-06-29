package com.Sistema.Backend.Pedidos.Repository;

import com.Sistema.Backend.Pedidos.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Pedidos.Entity.EstadoPedido;
import com.Sistema.Backend.Pedidos.Entity.Pedido;
import com.Sistema.Backend.Reportes.Dto.VentasPorPeriodoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Para el monitor de cocina: ver pedidos pendientes o en preparación
    // Ordenados por fecha para atender al que llegó primero (FIFO)
    @Query("SELECT DISTINCT p FROM Pedido p " +
            "LEFT JOIN FETCH p.detalles d " +
            "LEFT JOIN FETCH d.producto " +
            "WHERE p.estado IN :estados " +
            "ORDER BY p.fechaCreacion ASC")
    List<Pedido> findByEstadoInOrderByFechaCreacionAsc(@Param("estados") List<EstadoPedido> estados);

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

    // Filtrar sumas solo de pedidos entregados/completados
    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.fechaCreacion BETWEEN :inicio AND :fin AND p.estado = 'ENTREGADO'")
    BigDecimal sumarIngresosExitosos(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.fechaCreacion BETWEEN :inicio AND :fin AND p.estado = 'CANCELADO'")
    long contarPedidosCancelados(LocalDateTime inicio, LocalDateTime fin);

    // Contar y sumar solo pedidos que sí se completaron con éxito
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.fechaCreacion BETWEEN :inicio AND :fin AND p.estado = 'ENTREGADO'")
    long contarPedidosExitosos(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Agrupación cronológica diaria para la gráfica de líneas
    @Query(value = "SELECT TO_CHAR(p.fecha_creacion, 'YYYY-MM-DD') AS etiquetaPeriodo, " +
            "SUM(p.total) AS totalVentas, " +
            "COUNT(p.id) AS cantidadPedidos " +
            "FROM pedidos p " +
            "WHERE p.fecha_creacion BETWEEN :inicio AND :fin AND p.estado = 'ENTREGADO' " +
            "GROUP BY TO_CHAR(p.fecha_creacion, 'YYYY-MM-DD') " +
            "ORDER BY etiquetaPeriodo ASC", nativeQuery = true)
    List<Object[]> obtenerVentasDiariasNativo(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    Optional<Pedido> findByCodigo(String codigo);

    Optional<Pedido> findByMesaIdAndEstadoIn(Long mesaId, List<EstadoPedido> estados);
}
