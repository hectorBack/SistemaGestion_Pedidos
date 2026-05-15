package com.Sistema.Backend.Services;

import com.Sistema.Backend.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Entity.EstadoPedido;
import com.Sistema.Backend.Entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PedidoService {

    // Registro de pedido desde el link del cliente
    PedidoResponseDTO crearPedido(PedidoRequestDTO request);

    // Listado para el monitor de cocina (Solo PENDIENTE y EN_COCINA)
    List<PedidoResponseDTO> obtenerPedidosActivos();

    // Cambio de estado (Ej: de EN_COCINA a LISTO)
    PedidoResponseDTO actualizarEstado(Long id, EstadoPedido nuevoEstado);

    // Consulta de un pedido específico
    PedidoResponseDTO buscarPorId(Long id);

    // --- NUEVOS MÉTODOS RECOMENDADOS ---

    /**
     * Historial para el administrador.
     * A diferencia de 'activos', este trae todo (Entregados, Cancelados, etc.)
     */
    List<PedidoResponseDTO> obtenerHistorialTodos();

    /**
     * Búsqueda rápida en el local.
     * Si el cliente llega preguntando por su pedido usando sus dígitos de WhatsApp.
     */
    List<PedidoResponseDTO> buscarPorWhatsapp(String whatsappFinal);

    /**
     * Cancelación lógica.
     * En lugar de borrarlo de la BD, solo cambiamos el estado a CANCELADO.
     */
    void cancelarPedido(Long id);

    /**
     * Reporte de ventas simple.
     * Para saber cuánto se ha vendido en el día.
     */
    BigDecimal calcularTotalVentasDelDia();

    /**
     * Filtro avanzado: Por estado (opcional) y rango de fechas
     */
    Page<PedidoResponseDTO> obtenerPedidosFiltrados(
            EstadoPedido estado,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Pageable pageable);
}
