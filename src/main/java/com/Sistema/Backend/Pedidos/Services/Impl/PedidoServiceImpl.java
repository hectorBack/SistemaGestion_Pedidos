package com.Sistema.Backend.Pedidos.Services.Impl;

import com.Sistema.Backend.Mesas.Entity.EstadoMesa;
import com.Sistema.Backend.Mesas.Entity.Mesa;
import com.Sistema.Backend.Mesas.Repository.MesaRepository;
import com.Sistema.Backend.Pedidos.Dto.Request.AgregarItemsRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Exception.BusinessException;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Pedidos.Mapper.PedidoMapper;
import com.Sistema.Backend.Pedidos.Dto.Request.ItemPedidoRequestDTO;
import com.Sistema.Backend.Pedidos.Entity.DetallePedido;
import com.Sistema.Backend.Pedidos.Entity.EstadoPedido;
import com.Sistema.Backend.Pedidos.Entity.Pedido;
import com.Sistema.Backend.Productos.Entity.Producto;
import com.Sistema.Backend.Promociones.Entity.Promocion;
import com.Sistema.Backend.Pedidos.Repository.PedidoRepository;
import com.Sistema.Backend.Productos.Repository.ProductoRepository;
import com.Sistema.Backend.Promociones.Repository.PromocionRepository;
import com.Sistema.Backend.Pedidos.Services.PedidoService;
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
    private final MesaRepository mesaRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, ProductoRepository productoRepository, PromocionRepository promocionRepository, PedidoMapper pedidoMapper, MesaRepository mesaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.promocionRepository = promocionRepository;
        this.pedidoMapper = pedidoMapper;
        this.mesaRepository = mesaRepository;
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

        // 🌟 NUEVA LÓGICA: Si viene un ID de mesa, lo asociamos al pedido
        if (request.getMesaId() != null) {
            Mesa mesa = mesaRepository.findById(request.getMesaId())
                    .orElseThrow(() -> new ResourceNotFoundException("La mesa con ID " + request.getMesaId() + " no existe"));
            pedido.setMesa(mesa);
        }
        return pedido;
    }

    private void procesarItemsDelPedido(PedidoRequestDTO request, Pedido pedido) {
        BigDecimal totalAcumulado = BigDecimal.ZERO;
        Promocion promocion = null;

        // 1. Validar y cargar la promoción si viene en el DTO
        if (request.getPromocionId() != null) {
            promocion = promocionRepository.findById(request.getPromocionId())
                    .orElseThrow(() -> new ResourceNotFoundException("La promoción con ID " + request.getPromocionId() + " no existe"));
        }

        // 1. Sumar los subtotales de los productos individuales
        for (var itemDto : request.getItems()) {
            Producto producto = productoRepository.findById(itemDto.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto ID " + itemDto.getProductoId() + " no encontrado"));

            if (!producto.isDisponible()) {
                throw new BusinessException("El producto " + producto.getNombre() + " no está disponible actualmente.");
            }

            DetallePedido detalle = crearDetalle(pedido, producto, itemDto);

            BigDecimal precioAplicado = producto.getPrecio();

            // 🌟 CORRECCIÓN AQUÍ: Validamos usando la existencia del producto de manera segura
            if (promocion != null && promocion.isActiva() && promocion.getProducto() != null) {

                // Obtenemos el ID de forma segura (funciona incluso con Proxies de Hibernate)
                Long productoPromoId = promocion.getProducto().getId();

                if (productoPromoId.equals(producto.getId())) {
                    BigDecimal valorDescuento = promocion.getValor();

                    if ("PORCENTAJE".equalsIgnoreCase(promocion.getTipoDescuento())) {
                        BigDecimal porcentaje = valorDescuento.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
                        BigDecimal descuento = precioAplicado.multiply(porcentaje);
                        precioAplicado = precioAplicado.subtract(descuento);
                    } else if ("FIJO".equalsIgnoreCase(promocion.getTipoDescuento())) {
                        precioAplicado = precioAplicado.subtract(valorDescuento);
                    }

                    // Seteamos el precio real cobrado ($94.50) en el detalle del pedido
                    detalle.setPrecioUnitario(precioAplicado.setScale(2, java.math.RoundingMode.HALF_UP));
                }
            }

            pedido.getDetalles().add(detalle);

            BigDecimal subtotal = precioAplicado.multiply(BigDecimal.valueOf(itemDto.getCantidad()));
            totalAcumulado = totalAcumulado.add(subtotal);
        }

        // 3. Aplicar descuento si la promoción era de tipo GENERAL (cuenta completa)
        if (promocion != null && promocion.isActiva() && promocion.getProducto() == null && totalAcumulado.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal valorDescuento = promocion.getValor();

            if ("PORCENTAJE".equalsIgnoreCase(promocion.getTipoDescuento())) {
                BigDecimal porcentaje = valorDescuento.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
                BigDecimal descuento = totalAcumulado.multiply(porcentaje);
                totalAcumulado = totalAcumulado.subtract(descuento);
            } else if ("FIJO".equalsIgnoreCase(promocion.getTipoDescuento())) {
                totalAcumulado = totalAcumulado.subtract(valorDescuento);
            }
        }

        // 4. Control de seguridad y asignación final
        if (totalAcumulado.compareTo(BigDecimal.ZERO) < 0) {
            totalAcumulado = BigDecimal.ZERO;
        }

        pedido.setTotal(totalAcumulado.setScale(2, java.math.RoundingMode.HALF_UP));
    }

        private DetallePedido crearDetalle (Pedido pedido, Producto
        producto, ItemPedidoRequestDTO itemDto){
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(itemDto.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());

            System.out.println("Nota recibida en el backend: " + itemDto.getNotas());
            detalle.setNotas(itemDto.getNotas());
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
        // 1. Forzar una búsqueda limpia que traiga la Mesa cargada (Eager o con Join si es necesario)
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido " + id + " no existe"));

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new BusinessException("No se puede cambiar el estado de un pedido cancelado.");
        }

        pedido.setEstado(nuevoEstado);

        // 🌟 LOG DE CONTROL 1: Ver si el backend realmente entra aquí y qué tiene adentro
        System.out.println("=== BACKEND: Procesando pedido ID: " + id + " con nuevo estado: " + nuevoEstado);
        if (pedido.getMesa() != null) {
            System.out.println("=== BACKEND: La mesa asociada es la número: " + pedido.getMesa().getNumero());
        } else {
            System.out.println("=== BACKEND WARN: ¡Este pedido NO tiene ninguna mesa asignada en la BD!");
        }

        // 2. Si pasa a ENTREGADO y tiene mesa, operamos de forma segura
        if (nuevoEstado == EstadoPedido.ENTREGADO && pedido.getMesa() != null) {
            var mesa = pedido.getMesa();

            // Cambiamos el estado de la mesa
            mesa.setEstado(EstadoMesa.SUCIA);

            // IMPORTANTE: Primero guardamos y confirmamos la mesa de forma independiente
            mesaRepository.saveAndFlush(mesa);

            // Opcional: Si en tu entidad Mesa tienes una lista de pedidos, o si en Pedido
            // la relación requiere actualizar ambos lados, asegúrate de refrescarlo.
            log.info("¡Mesa {} liberada con éxito en la base de datos!", mesa.getNumero());
        }

        // Guardamos el pedido
        Pedido pedidoActualizado = pedidoRepository.saveAndFlush(pedido);
        return pedidoMapper.toResponseDTO(pedidoActualizado);
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
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se puede cancelar: Pedido no encontrado"));

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaActualizacion(LocalDateTime.now());

        // 🌟 Opcional: También liberar la mesa si se cancela la comanda entera
        if (pedido.getMesa() != null) {
            var mesa = pedido.getMesa();
            mesa.setEstado(EstadoMesa.LIBRE); // O el valor que uses para libre
            mesaRepository.save(mesa);
        }

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

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorCodigo(String codigo) {
        return pedidoRepository.findByCodigo(codigo)
                .map(pedidoMapper::toResponseDTO) // Convierte elegantemente de Entidad a DTO
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con el código: " + codigo));
    }

    @Override
    @Transactional
    public PedidoResponseDTO agregarItemsAPedido(Long pedidoId, AgregarItemsRequestDTO request) {
        // 1. Buscar el pedido existente
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // Validar que el pedido aún se pueda modificar
        if (pedido.getEstado() == EstadoPedido.ENTREGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("No se pueden agregar productos a un pedido finalizado o cancelado");
        }

        BigDecimal subtotalNuevosItems = BigDecimal.ZERO;

        // 2. Recorrer los nuevos productos enviados desde el Frontend
        for (ItemPedidoRequestDTO itemDto : request.getNuevosItems()) {
            Producto producto = productoRepository.findById(itemDto.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            // 3. Crear el nuevo detalle usando tu método existente
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(itemDto.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setNotas(itemDto.getNotas());
            detalle.setEnviadoACocina(false); // 🌟 Es nuevo, va para la cocina

            // Calcular el acumulado del dinero nuevo
            BigDecimal costoItem = producto.getPrecio().multiply(BigDecimal.valueOf(itemDto.getCantidad()));
            subtotalNuevosItems = subtotalNuevosItems.add(costoItem);

            // 4. Agregarlo a la lista de detalles del pedido original
            pedido.getDetalles().add(detalle);
        }

        // 5. Actualizar el gran total del pedido
        pedido.setTotal(pedido.getTotal().add(subtotalNuevosItems));

        // 6. Guardar los cambios (CascadeType.ALL se encarga de insertar los nuevos detalles automáticamente)
        Pedido pedidoActualizado = pedidoRepository.save(pedido);

        // 7. Retornar el pedido con el Mapper que ya tienes listo
        return pedidoMapper.toResponseDTO(pedidoActualizado);
    }

    @Override
    public PedidoResponseDTO obtenerPedidoActivoPorMesa(Long mesaId) {
        List<EstadoPedido> estadosActivos = List.of(EstadoPedido.PENDIENTE, EstadoPedido.EN_COCINA);

        Pedido pedido = pedidoRepository.findByMesaIdAndEstadoIn(mesaId, estadosActivos)
                .orElseThrow(() -> new ResourceNotFoundException("No hay pedidos activos para esta mesa"));

        return pedidoMapper.toResponseDTO(pedido);
    }
}
