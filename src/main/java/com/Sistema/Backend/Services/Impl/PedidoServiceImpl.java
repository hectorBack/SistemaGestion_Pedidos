package com.Sistema.Backend.Services.Impl;

import com.Sistema.Backend.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Entity.DetallePedido;
import com.Sistema.Backend.Entity.EstadoPedido;
import com.Sistema.Backend.Entity.Pedido;
import com.Sistema.Backend.Entity.Producto;
import com.Sistema.Backend.Exception.BusinessException;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Mapper.PedidoMapper;
import com.Sistema.Backend.Repository.PedidoRepository;
import com.Sistema.Backend.Repository.ProductoRepository;
import com.Sistema.Backend.Services.PedidoService;
import jakarta.persistence.EntityNotFoundException;
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
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final PedidoMapper pedidoMapper;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, ProductoRepository productoRepository, PedidoMapper pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
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

        for (var itemDto : request.getItems()) {
            // 1. Buscamos el producto (Podrías optimizar esto con una sola consulta IN si la lista crece mucho)
            Producto producto = productoRepository.findById(itemDto.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto ID " + itemDto.getProductoId() + " no encontrado"));

            // 2. Lógica de validación de negocio adicional (Ejemplo: disponibilidad)
            if (!producto.isDisponible()) {
                throw new BusinessException("El producto " + producto.getNombre() + " no está disponible actualmente.");
            }

            // 3. Crear detalle (Encapsulamos la creación)
            DetallePedido detalle = crearDetalle(pedido, producto, itemDto);
            pedido.getDetalles().add(detalle);

            // 4. Sumar al total
            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(itemDto.getCantidad()));
            totalAcumulado = totalAcumulado.add(subtotal);
        }
        pedido.setTotal(totalAcumulado);
    }

    private DetallePedido crearDetalle(Pedido pedido, Producto producto, com.Sistema.Backend.Dto.Request.ItemPedidoRequestDTO itemDto) {
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
