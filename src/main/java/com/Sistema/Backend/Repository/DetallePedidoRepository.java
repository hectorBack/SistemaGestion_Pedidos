package com.Sistema.Backend.Repository;

import com.Sistema.Backend.Entity.DetallePedido;
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
}
