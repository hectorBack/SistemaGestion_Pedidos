package com.Sistema.Backend.Pedidos.Repository;

import com.Sistema.Backend.Pedidos.Entity.DetallePedido;
import com.Sistema.Backend.Reportes.Dto.VentasPorCategoriaDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    // Consulta para obtener el nombre del producto más vendido
    @Query("SELECT d.producto.nombre FROM DetallePedido d " +
            "WHERE d.pedido.fechaCreacion BETWEEN :inicio AND :fin " +
            "AND d.pedido.estado != 'CANCELADO' " +
            "GROUP BY d.producto.nombre " +
            "ORDER BY SUM(d.cantidad) DESC")
    List<String> encontrarProductoMasVendido(@Param("inicio") LocalDateTime inicio,
                                             @Param("fin") LocalDateTime fin,
                                             Pageable pageable);

    // Consulta para contar el total de productos físicos vendidos
    @Query("SELECT SUM(d.cantidad) FROM DetallePedido d " +
            "WHERE d.pedido.fechaCreacion BETWEEN :inicio AND :fin " +
            "AND d.pedido.estado != 'CANCELADO'")
    Long contarTotalProductosVendidos(@Param("inicio") LocalDateTime inicio,
                                      @Param("fin") LocalDateTime fin);

    // Cambiamos a Consulta Nativa (nativeQuery = true) para evitar problemas de validación con los DTOs
    @Query(value = "SELECT c.nombre AS categoria, " +
            "SUM(dp.cantidad) AS cantidadVendida, " +
            "SUM(dp.precio_unitario * dp.cantidad) AS totalRecaudado " +
            "FROM detalle_pedidos dp " + // Asegúrate de que así se llame tu tabla intermedia en Postgres
            "JOIN productos p ON dp.producto_id = p.id " +
            "JOIN categorias c ON p.categoria_id = c.id " +
            "JOIN pedidos ped ON dp.pedido_id = ped.id " +
            "WHERE ped.fecha_creacion BETWEEN :inicio AND :fin AND ped.estado = 'ENTREGADO' " +
            "GROUP BY c.nombre", nativeQuery = true)
    List<Object[]> obtenerVentasPorCategoriaNativo(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Cambiar el Top 1 por un Top 5 dinámico usando el Pageable que ya tienes
    @Query("SELECT p.nombre FROM DetallePedido dp JOIN dp.producto p JOIN dp.pedido ped " +
            "WHERE ped.fechaCreacion BETWEEN :inicio AND :fin AND ped.estado = 'ENTREGADO' " +
            "GROUP BY p.nombre ORDER BY SUM(dp.cantidad) DESC")
    List<String> encontrarTopProductos(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, Pageable pageable);

    // Top de productos más vendidos (Paginado desde Service para obtener un Top 5 o Top 10)
    @Query("SELECT p.nombre FROM DetallePedido dp JOIN dp.producto p JOIN dp.pedido ped " +
            "WHERE ped.fechaCreacion BETWEEN :inicio AND :fin AND ped.estado = 'ENTREGADO' " +
            "GROUP BY p.nombre ORDER BY SUM(dp.cantidad) DESC")
    List<String> encontrarProductosMasVendidos(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, Pageable pageable);
}
