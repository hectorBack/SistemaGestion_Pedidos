package com.Sistema.Backend.Services.Impl;

import com.Sistema.Backend.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Entity.*;
import com.Sistema.Backend.Exception.BusinessException;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Mapper.PedidoMapper;
import com.Sistema.Backend.Repository.PedidoRepository;
import com.Sistema.Backend.Repository.ProductoRepository;
import com.Sistema.Backend.Repository.PromocionRepository;
import com.Sistema.Backend.Services.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final PromocionRepository promocionRepository;
    private final PedidoMapper pedidoMapper;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, ProductoRepository productoRepository, PromocionRepository promocionRepository, PedidoMapper pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.promocionRepository = promocionRepository;
        this.pedidoMapper = pedidoMapper;
    }

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO request) {
        Pedido pedido = inicializarNuevoPedido(request);

        // Optimizamos: Validamos productos y calculamos total
        procesarItemsDelPedido(request, pedido);

        return pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    // --- MÉTODOS PRIVADOS PARA MANTENIBILIDAD ---

    private Pedido inicializarNuevoPedido(PedidoRequestDTO request) {
        Pedido pedido = new Pedido();
        pedido.setWhatsappFinal(request.getWhatsappFinal());
        pedido.setNombreCliente(request.getNombreCliente());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDetalles(new ArrayList<>()); // Aseguramos inicialización
        return pedido;
    }

    private void procesarItemsDelPedido(PedidoRequestDTO request, Pedido pedido) {
        BigDecimal totalAcumulado = BigDecimal.ZERO;

        // 1. Sumar los subtotales de los productos individuales
        for (var itemDto : request.getItems()) {
            Producto producto = productoRepository.findById(itemDto.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto ID " + itemDto.getProductoId() + " no encontrado"));

            if (!producto.isDisponible()) {
                throw new BusinessException("El producto " + producto.getNombre() + " no está disponible actualmente.");
            }

            DetallePedido detalle = crearDetalle(pedido, producto, itemDto);
            pedido.getDetalles().add(detalle);

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(itemDto.getCantidad()));
            totalAcumulado = totalAcumulado.add(subtotal);
        }

        // 2. Aplicar descuento general si se envió una promoción
        if (request.getPromocionId() != null && totalAcumulado.compareTo(BigDecimal.ZERO) > 0) {

            Promocion promocion = promocionRepository.findById(request.getPromocionId())
                    .orElseThrow(() -> new ResourceNotFoundException("La promoción con ID " + request.getPromocionId() + " no existe"));


            // 🚨 AQUÍ SUELE ESTAR EL FILTRO DE SEGURIDAD
            if (promocion.isActiva() && promocion.getProducto() == null) {

                BigDecimal valorDescuento = promocion.getValor();

                if ("PORCENTAJE".equalsIgnoreCase(promocion.getTipoDescuento())) {
                    BigDecimal porcentaje = valorDescuento.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
                    BigDecimal descuento = totalAcumulado.multiply(porcentaje);
                    totalAcumulado = totalAcumulado.subtract(descuento);
                } else if ("FIJO".equalsIgnoreCase(promocion.getTipoDescuento())) {
                    totalAcumulado = totalAcumulado.subtract(valorDescuento);
                }
            }

            if (totalAcumulado.compareTo(BigDecimal.ZERO) < 0) {
                totalAcumulado = BigDecimal.ZERO;
            }

            pedido.setTotal(totalAcumulado.setScale(2, java.math.RoundingMode.HALF_UP));
        }
    }

        private DetallePedido crearDetalle (Pedido pedido, Producto
        producto, com.Sistema.Backend.Dto.Request.ItemPedidoRequestDTO itemDto){
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(itemDto.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setNotasPersonalizacion(itemDto.getNotas());
            return detalle;
        }


    @Override
    public List<PedidoResponseDTO> obtenerPedidosActivos() {
        return pedidoRepository.findByEstadoInOrderByFechaCreacionAsc(
                        Arrays.asList(EstadoPedido.PENDIENTE, EstadoPedido.EN_COCINA)
                ).stream()
                .map(pedido -> pedidoMapper.toResponseDTO(pedido))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PedidoResponseDTO actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido " + id + " no existe"));

        // Lógica de escalabilidad: No puedes pasar de CANCELADO a ENTREGADO
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new BusinessException("No se puede cambiar el estado de un pedido cancelado.");
        }

        pedido.setEstado(nuevoEstado);
        return pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoResponseDTO buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        return pedidoMapper.toResponseDTO(pedido);
    }

    @Override
    public List<PedidoResponseDTO> obtenerHistorialTodos() {
        // Traemos absolutamente todos los pedidos, ordenados por los más recientes primero
        return pedidoRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"))
                .stream()
                .map(pedidoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidoResponseDTO> buscarPorWhatsapp(String whatsappFinal) {
        return pedidoRepository.findByWhatsappFinal(whatsappFinal).stream()
                .map(pedidoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelarPedido(Long id) {
        // Reutilizamos el método de buscar para asegurar que existe
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se puede cancelar: Pedido no encontrado"));

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    @Override
    public BigDecimal calcularTotalVentasDelDia() {
        BigDecimal total = pedidoRepository.sumarTotalVentasPorPeriodo(
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Page<PedidoResponseDTO> obtenerPedidosFiltrados(
            EstadoPedido estado,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Pageable pageable) {

        // Convertimos LocalDate a LocalDateTime (de 00:00:00 a 23:59:59)
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        // Llamamos al repository (Usa Entidades)
        Page<Pedido> pedidosPage = pedidoRepository.filtrarPedidos(estado, inicio, fin, pageable);

        // Convertimos el Page de Entidades a Page de DTOs
        return pedidosPage.map(pedidoMapper::toResponseDTO);
    }
}
