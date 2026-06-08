package com.Sistema.Backend.Pagos.Services.Impl;

import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Pagos.Dto.Request.PagoRequestDTO;
import com.Sistema.Backend.Pagos.Dto.Response.PagoResponseDTO;
import com.Sistema.Backend.Pagos.Entity.EstadoPago;
import com.Sistema.Backend.Pagos.Entity.MetodoPago;
import com.Sistema.Backend.Pagos.Entity.Pago;
import com.Sistema.Backend.Pagos.Mapper.PagoMapper;
import com.Sistema.Backend.Pagos.Repository.PagoRepository;
import com.Sistema.Backend.Pagos.Services.PagoService;
import com.Sistema.Backend.Pedidos.Entity.EstadoPedido;
import com.Sistema.Backend.Pedidos.Entity.Pedido;
import com.Sistema.Backend.Pedidos.Repository.PedidoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
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
        log.info("Iniciando registro de pago para el Pedido ID: {}. Método de pago: {}", dto.getPedidoId(), dto.getMetodoPago());
        // 1. Validar que el pedido exista
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> {
                    log.error("Fallo al registrar pago: El pedido ID {} no existe", dto.getPedidoId());
                    return new ResourceNotFoundException("Pedido no encontrado con ID: " + dto.getPedidoId());
                });

        // 2. Mapear DTO a la entidad base
        Pago pago = pagoMapper.toEntity(dto);
        pago.setPedido(pedido);

        // 3. Asignamos estado (Por defecto APROBADO para flujos directos/efectivo de mostrador)
        pago.setEstado(EstadoPago.APROBADO);

        // 4. Guardar la transacción de Pago
        Pago pagoGuardado = pagoRepository.save(pago);
        log.info("Transacción de pago guardada exitosamente. ID Pago: {}, Código de Transacción: '{}'",
                pagoGuardado.getId(), pagoGuardado.getCodigoTransaccion());

        // 5. 🚀 REGLA DE NEGOCIO: Si el pago se aprueba, el pedido avanza automáticamente a la cocina
        if (pagoGuardado.getEstado() == EstadoPago.APROBADO) {
            log.info("PAGO APROBADO detectado de forma automática. Avanzando estado del Pedido ID: {} a EN_COCINA", pedido.getId());
            pedido.setEstado(EstadoPedido.EN_COCINA);
            pedidoRepository.save(pedido);
        }

        return pagoMapper.toResponseDTO(pagoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO obtenerPorCodigo(String codigoTransaccion) {
        log.info("Buscando detalles de pago mediante código público: '{}'", codigoTransaccion);
        return pagoRepository.findByCodigoTransaccion(codigoTransaccion)
                .map(pagoMapper::toResponseDTO)
                .orElseThrow(() -> {
                    log.error("Auditoría: Intento fallido al localizar la transacción '{}'", codigoTransaccion);
                    return new ResourceNotFoundException("Transacción de pago no encontrada: " + codigoTransaccion);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO obtenerPorPedidoId(Long pedidoId) {
        log.info("Buscando historial de pago asociado al Pedido ID: {}", pedidoId);
        return pagoRepository.findByPedidoId(pedidoId)
                .map(pagoMapper::toResponseDTO)
                .orElseThrow(() -> {
                    log.warn("No se localizó ningún pago vinculado al Pedido ID: {}", pedidoId);
                    return new ResourceNotFoundException("No se encontró ningún pago asociado al pedido con ID: " + pedidoId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> obtenerTodos() {
        log.info("Solicitando listado global histórico de pagos procesados");
        return pagoRepository.findAll().stream()
                .map(pagoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoResponseDTO> obtenerPagosFiltrados(MetodoPago metodo, LocalDate inicio, LocalDate fin, Pageable pageable) {
        // Ajustamos los rangos de tiempo de forma exacta
        LocalDateTime fechaInicio = inicio.atStartOfDay(); // YYYY-MM-DD 00:00:00
        LocalDateTime fechaFin = fin.atTime(LocalTime.MAX); // YYYY-MM-DD 23:59:59.999999

        log.info("Consulta de reportería de pagos -> Método: {}, Rango: [{} - {}] | Pág: {}, Tamaño: {}",
                metodo, fechaInicio, fechaFin, pageable.getPageNumber(), pageable.getPageSize());

        return pagoRepository.filtrarPagos(metodo, fechaInicio, fechaFin, pageable)
                .map(pagoMapper::toResponseDTO);
    }
}
