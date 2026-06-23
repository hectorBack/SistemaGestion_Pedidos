package com.Sistema.Backend.Pagos.Services;

import com.Sistema.Backend.Pagos.Dto.ReembolsoRequestDTO;
import com.Sistema.Backend.Pagos.Dto.Request.PagoRequestDTO;
import com.Sistema.Backend.Pagos.Dto.Response.HistorialPagosResponseDTO;
import com.Sistema.Backend.Pagos.Dto.Response.PagoResponseDTO;
import com.Sistema.Backend.Pagos.Entity.MetodoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;


public interface PagoService {

    // Registrar un nuevo pago (Caja o Pasarela)
    PagoResponseDTO registrarPago(PagoRequestDTO pagoRequestDTO);

    // Consultar pago por su código transaccional
    PagoResponseDTO obtenerPorCodigo(String codigoTransaccion);

    // Consultar el pago de un pedido específico
    PagoResponseDTO obtenerPorPedidoId(Long pedidoId);

    // Listar todos los pagos realizados
    List<PagoResponseDTO> obtenerTodos();

    // Nuevo método con filtros de negocio y paginador
    HistorialPagosResponseDTO obtenerPagosFiltrados(MetodoPago metodo, LocalDate inicio, LocalDate fin, Pageable pageable);

    PagoResponseDTO reembolsarPago(Long id, ReembolsoRequestDTO reembolsoRequest);
}
