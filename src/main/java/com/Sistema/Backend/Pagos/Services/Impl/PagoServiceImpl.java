package com.Sistema.Backend.Pagos.Services.Impl;

import com.Sistema.Backend.Pagos.Dto.Request.PagoRequestDTO;
import com.Sistema.Backend.Pagos.Dto.Response.PagoResponseDTO;
import com.Sistema.Backend.Pagos.Entity.EstadoPago;
import com.Sistema.Backend.Pagos.Entity.Pago;
import com.Sistema.Backend.Pagos.Mapper.PagoMapper;
import com.Sistema.Backend.Pagos.Repository.PagoRepository;
import com.Sistema.Backend.Pagos.Services.PagoService;
import com.Sistema.Backend.Pedidos.Entity.EstadoPedido;
import com.Sistema.Backend.Pedidos.Entity.Pedido;
import com.Sistema.Backend.Pedidos.Repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final PagoMapper pagoMapper;

    public PagoServiceImpl(PagoRepository pagoRepository, PedidoRepository pedidoRepository, PagoMapper pagoMapper) {
        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagoMapper = pagoMapper;
    }

    @Override
    @Transactional
    public PagoResponseDTO registrarPago(PagoRequestDTO dto) {
        // 1. Validar que el pedido exista
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + dto.getPedidoId()));

        // 2. Mapear DTO a la entidad base
        Pago pago = pagoMapper.toEntity(dto);
        pago.setPedido(pedido);

        // 3. Asignamos estado (Por defecto APROBADO para flujos directos/efectivo de mostrador)
        pago.setEstado(EstadoPago.APROBADO);

        // 4. Guardar la transacción de Pago
        Pago pagoGuardado = pagoRepository.save(pago);

        // 5. 🚀 REGLA DE NEGOCIO: Si el pago se aprueba, el pedido avanza automáticamente a la cocina
        if (pagoGuardado.getEstado() == EstadoPago.APROBADO) {
            pedido.setEstado(EstadoPedido.EN_COCINA);
            pedidoRepository.save(pedido);
        }

        return pagoMapper.toResponseDTO(pagoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO obtenerPorCodigo(String codigoTransaccion) {
        return pagoRepository.findByCodigoTransaccion(codigoTransaccion)
                .map(pagoMapper::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Transacción de pago no encontrada: " + codigoTransaccion));
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO obtenerPorPedidoId(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId)
                .map(pagoMapper::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("No se encontró ningún pago asociado al pedido con ID: " + pedidoId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> obtenerTodos() {
        return pagoRepository.findAll().stream()
                .map(pagoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
