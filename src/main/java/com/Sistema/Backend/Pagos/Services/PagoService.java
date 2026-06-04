package com.Sistema.Backend.Pagos.Services;

import com.Sistema.Backend.Pagos.Dto.Request.PagoRequestDTO;
import com.Sistema.Backend.Pagos.Dto.Response.PagoResponseDTO;

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
}
