package com.Sistema.Backend.Services.Impl;

import com.Sistema.Backend.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Entity.DetallePedido;
import com.Sistema.Backend.Entity.EstadoPedido;
import com.Sistema.Backend.Entity.Pedido;
import com.Sistema.Backend.Entity.Producto;
import com.Sistema.Backend.Mapper.PedidoMapper;
import com.Sistema.Backend.Repository.PedidoRepository;
import com.Sistema.Backend.Repository.ProductoRepository;
import com.Sistema.Backend.Services.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        Pedido pedido = new Pedido();
        pedido.setWhatsappFinal(request.getWhatsappFinal());
        pedido.setNombreCliente(request.getNombreCliente());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        BigDecimal totalAcumulado = BigDecimal.ZERO;

        // Convertir RequestDTO.Items a Entidades DetallePedido
        for (var itemDto : request.getItems()) {
            Producto producto = productoRepository.findById(itemDto.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(itemDto.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setNotasPersonalizacion(itemDto.getNotas());

            BigDecimal subtotal = producto.getPrecio().multiply(new BigDecimal(itemDto.getCantidad()));
            totalAcumulado = totalAcumulado.add(subtotal);

            pedido.getDetalles().add(detalle);
        }

        pedido.setTotal(totalAcumulado);
        Pedido guardado = pedidoRepository.save(pedido);

        return pedidoMapper.toResponseDTO(guardado);
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
    public PedidoResponseDTO actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

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
}
