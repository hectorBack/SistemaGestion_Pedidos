package com.Sistema.Backend.Repository;

import com.Sistema.Backend.Entity.EstadoPedido;
import com.Sistema.Backend.Entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Para el monitor de cocina: ver pedidos pendientes o en preparación
    // Ordenados por fecha para atender al que llegó primero (FIFO)
    List<Pedido> findByEstadoInOrderByFechaCreacionAsc(List<EstadoPedido> estados);

    // Para buscar rápidamente por los últimos dígitos de WhatsApp
    List<Pedido> findByWhatsappFinal(String whatsappFinal);
}
