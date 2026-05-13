package com.Sistema.Backend.Repository;

import com.Sistema.Backend.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Entity.EstadoPedido;
import com.Sistema.Backend.Entity.Pedido;
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

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.fechaCreacion >= :inicio AND p.fechaCreacion <= :fin AND p.estado = 'ENTREGADO'")
    BigDecimal sumarTotalVentasPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
