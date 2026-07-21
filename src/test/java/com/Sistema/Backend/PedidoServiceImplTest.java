package com.Sistema.Backend;

import com.Sistema.Backend.Exception.BadRequestException;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Mesas.Entity.EstadoMesa;
import com.Sistema.Backend.Mesas.Entity.Mesa;
import com.Sistema.Backend.Mesas.Repository.MesaRepository;
import com.Sistema.Backend.Pedidos.Dto.Request.AgregarItemsRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Request.ItemPedidoRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Pedidos.Entity.EstadoPedido;
import com.Sistema.Backend.Pedidos.Entity.Pedido;
import com.Sistema.Backend.Pedidos.Mapper.PedidoMapper;
import com.Sistema.Backend.Pedidos.Repository.PedidoRepository;
import com.Sistema.Backend.Pedidos.Services.Impl.PedidoServiceImpl;
import com.Sistema.Backend.Productos.Entity.Producto;
import com.Sistema.Backend.Productos.Repository.ProductoRepository;
import com.Sistema.Backend.Promociones.Entity.Promocion;
import com.Sistema.Backend.Promociones.Repository.PromocionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    @Mock
    private MesaRepository mesaRepository;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Pedido pedido;
    private Mesa mesa;
    private Producto producto;
    private ItemPedidoRequestDTO itemDto;
    private PedidoRequestDTO pedidoRequestDTO;
    private PedidoResponseDTO pedidoResponseDTO;

    @BeforeEach
    void setUp() {
        mesa = new Mesa();
        mesa.setId(1L);
        mesa.setNumero("5");
        mesa.setEstado(EstadoMesa.LIBRE);

        producto = new Producto();
        producto.setId(100L);
        producto.setNombre("Hamburguesa Doble");
        producto.setPrecio(new BigDecimal("100.00"));
        producto.setDisponible(true);

        itemDto = new ItemPedidoRequestDTO();
        itemDto.setProductoId(100L);
        itemDto.setCantidad(2);
        itemDto.setNotas("Sin cebolla");
        itemDto.setSabores(List.of("Picante", "BBQ"));

        pedidoRequestDTO = new PedidoRequestDTO();
        pedidoRequestDTO.setNombreCliente("Juan Pérez");
        pedidoRequestDTO.setWhatsappFinal("123456789");
        pedidoRequestDTO.setMesaId(1L);
        pedidoRequestDTO.setItems(List.of(itemDto));

        pedido = new Pedido();
        pedido.setId(10L);
        pedido.setNombreCliente("Juan Pérez");
        pedido.setWhatsappFinal("123456789");
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(new BigDecimal("200.00"));
        pedido.setDetalles(new ArrayList<>());

        pedidoResponseDTO = new PedidoResponseDTO();
        pedidoResponseDTO.setId(10L);
        pedidoResponseDTO.setNombreCliente("Juan Pérez");
        pedidoResponseDTO.setTotal(new BigDecimal("200.00"));
    }

    // =========================================================================
    // 1. PRUEBAS PARA: crearPedido()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para crearPedido")
    class CrearPedidoTests {

        @Test
        @DisplayName("Debe crear pedido exitosamente asociando mesa y cambiando mesa a OCUPADA")
        void crearPedido_ExitoConMesa() {
            // Given
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));
            given(productoRepository.findById(100L)).willReturn(Optional.of(producto));
            given(pedidoRepository.save(any(Pedido.class))).willReturn(pedido);
            given(pedidoMapper.toResponseDTO(pedido)).willReturn(pedidoResponseDTO);

            // When
            PedidoResponseDTO resultado = pedidoService.crearPedido(pedidoRequestDTO);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(pedidoResponseDTO);
            assertThat(mesa.getEstado()).isEqualTo(EstadoMesa.OCUPADA);

            then(mesaRepository).should(times(1)).save(mesa);
            then(pedidoRepository).should(times(1)).save(any(Pedido.class));
        }

        @Test
        @DisplayName("Debe aplicar descuento PORCENTUAL de promoción por producto específico")
        void crearPedido_ConPromocionPorcentualProducto() {
            // Given
            Promocion promo = new Promocion();
            promo.setId(5L);
            promo.setActiva(true);
            promo.setTipoDescuento("PORCENTAJE");
            promo.setValor(new BigDecimal("10.00")); // 10%
            promo.setProducto(producto);

            pedidoRequestDTO.setPromocionId(5L);

            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));
            given(promocionRepository.findById(5L)).willReturn(Optional.of(promo));
            given(productoRepository.findById(100L)).willReturn(Optional.of(producto));
            given(pedidoRepository.save(any(Pedido.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(pedidoMapper.toResponseDTO(any(Pedido.class))).willReturn(pedidoResponseDTO);

            // When
            PedidoResponseDTO resultado = pedidoService.crearPedido(pedidoRequestDTO);

            // Then
            assertThat(resultado).isNotNull();
            then(pedidoRepository).should(times(1)).save(any(Pedido.class));
        }

        @Test
        @DisplayName("Debe aplicar descuento GENERAL FIJO a la cuenta entera")
        void crearPedido_ConPromocionGeneralFija() {
            // Given
            Promocion promo = new Promocion();
            promo.setId(6L);
            promo.setActiva(true);
            promo.setTipoDescuento("FIJO");
            promo.setValor(new BigDecimal("50.00"));
            promo.setProducto(null); // Descuento General

            pedidoRequestDTO.setPromocionId(6L);

            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));
            given(promocionRepository.findById(6L)).willReturn(Optional.of(promo));
            given(productoRepository.findById(100L)).willReturn(Optional.of(producto));
            given(pedidoRepository.save(any(Pedido.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(pedidoMapper.toResponseDTO(any(Pedido.class))).willReturn(pedidoResponseDTO);

            // When
            PedidoResponseDTO resultado = pedidoService.crearPedido(pedidoRequestDTO);

            // Then
            assertThat(resultado).isNotNull();
            then(pedidoRepository).should(times(1)).save(any(Pedido.class));
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si un producto del pedido no está disponible")
        void crearPedido_ProductoNoDisponible_LanzaExcepcion() {
            // Given
            producto.setDisponible(false);
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));
            given(productoRepository.findById(100L)).willReturn(Optional.of(producto));

            // When / Then
            assertThatThrownBy(() -> pedidoService.crearPedido(pedidoRequestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("no está disponible actualmente");

            then(pedidoRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si la mesa indicada no existe")
        void crearPedido_MesaNoExiste_LanzaExcepcion() {
            // Given
            given(mesaRepository.findById(1L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> pedidoService.crearPedido(pedidoRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("La mesa con ID 1 no existe");

            then(pedidoRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 2. PRUEBAS PARA: actualizarEstado()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para actualizarEstado")
    class ActualizarEstadoTests {

        @Test
        @DisplayName("Debe cambiar estado a ENTREGADO y liberar la mesa dejándola SUCIA")
        void actualizarEstado_AEntregado_MarcaMesaSucia() {
            // Given
            pedido.setMesa(mesa);
            given(pedidoRepository.findById(10L)).willReturn(Optional.of(pedido));
            given(pedidoRepository.saveAndFlush(pedido)).willReturn(pedido);
            given(pedidoMapper.toResponseDTO(pedido)).willReturn(pedidoResponseDTO);

            // When
            PedidoResponseDTO resultado = pedidoService.actualizarEstado(10L, EstadoPedido.ENTREGADO);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.ENTREGADO);
            assertThat(mesa.getEstado()).isEqualTo(EstadoMesa.SUCIA);

            then(mesaRepository).should(times(1)).saveAndFlush(mesa);
            then(pedidoRepository).should(times(1)).saveAndFlush(pedido);
        }

        @Test
        @DisplayName("Debe cambiar estado a CANCELADO y liberar la mesa dejándola LIBRE")
        void actualizarEstado_ACancelado_MarcaMesaLibre() {
            // Given
            pedido.setMesa(mesa);
            given(pedidoRepository.findById(10L)).willReturn(Optional.of(pedido));
            given(pedidoRepository.saveAndFlush(pedido)).willReturn(pedido);
            given(pedidoMapper.toResponseDTO(pedido)).willReturn(pedidoResponseDTO);

            // When
            PedidoResponseDTO resultado = pedidoService.actualizarEstado(10L, EstadoPedido.CANCELADO);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
            assertThat(mesa.getEstado()).isEqualTo(EstadoMesa.LIBRE);

            then(mesaRepository).should(times(1)).saveAndFlush(mesa);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el pedido ya se encuentra cancelado")
        void actualizarEstado_PedidoYaCancelado_LanzaExcepcion() {
            // Given
            pedido.setEstado(EstadoPedido.CANCELADO);
            given(pedidoRepository.findById(10L)).willReturn(Optional.of(pedido));

            // When / Then
            assertThatThrownBy(() -> pedidoService.actualizarEstado(10L, EstadoPedido.EN_COCINA))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("No se puede cambiar el estado de un pedido cancelado.");

            then(pedidoRepository).should(never()).saveAndFlush(any());
        }
    }

    // =========================================================================
    // 3. PRUEBAS PARA: agregarItemsAPedido()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para agregarItemsAPedido")
    class AgregarItemsAPedidoTests {

        @Test
        @DisplayName("Debe agregar nuevos ítems al pedido y recalcular el gran total acumulado")
        void agregarItemsAPedido_Exito() {
            // Given
            AgregarItemsRequestDTO request = new AgregarItemsRequestDTO();
            ItemPedidoRequestDTO nuevoItem = new ItemPedidoRequestDTO();
            nuevoItem.setProductoId(100L);
            nuevoItem.setCantidad(1);
            request.setNuevosItems(List.of(nuevoItem));

            given(pedidoRepository.findById(10L)).willReturn(Optional.of(pedido));
            given(productoRepository.findById(100L)).willReturn(Optional.of(producto));
            given(pedidoRepository.save(pedido)).willReturn(pedido);
            given(pedidoMapper.toResponseDTO(pedido)).willReturn(pedidoResponseDTO);

            // When
            PedidoResponseDTO resultado = pedidoService.agregarItemsAPedido(10L, request);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(pedido.getTotal()).isEqualTo(new BigDecimal("300.00")); // 200 inicial + 100 del nuevo ítem
            assertThat(pedido.getDetalles()).hasSize(1);

            then(pedidoRepository).should(times(1)).save(pedido);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si se intenta agregar ítems a un pedido ENTREGADO")
        void agregarItemsAPedido_PedidoEntregado_LanzaExcepcion() {
            // Given
            pedido.setEstado(EstadoPedido.ENTREGADO);
            given(pedidoRepository.findById(10L)).willReturn(Optional.of(pedido));

            // When / Then
            assertThatThrownBy(() -> pedidoService.agregarItemsAPedido(10L, new AgregarItemsRequestDTO()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("No se pueden agregar productos a un pedido finalizado o cancelado");

            then(pedidoRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 4. PRUEBAS PARA: Consultas y Búsquedas
    // =========================================================================
    @Nested
    @DisplayName("Pruebas de consultas y búsquedas")
    class ConsultasTests {

        @Test
        @DisplayName("Debe obtener pedidos activos (PENDIENTE o EN_COCINA)")
        void obtenerPedidosActivos_Exito() {
            // Given
            List<EstadoPedido> estados = List.of(EstadoPedido.PENDIENTE, EstadoPedido.EN_COCINA);
            given(pedidoRepository.findByEstadoInOrderByFechaCreacionAsc(estados)).willReturn(List.of(pedido));
            given(pedidoMapper.toResponseDTO(pedido)).willReturn(pedidoResponseDTO);

            // When
            List<PedidoResponseDTO> resultado = pedidoService.obtenerPedidosActivos();

            // Then
            assertThat(resultado).hasSize(1).contains(pedidoResponseDTO);
            then(pedidoRepository).should(times(1)).findByEstadoInOrderByFechaCreacionAsc(estados);
        }

        @Test
        @DisplayName("Debe buscar pedido activo por ID de mesa")
        void obtenerPedidoActivoPorMesa_Exito() {
            // Given
            List<EstadoPedido> estadosActivos = List.of(EstadoPedido.PENDIENTE, EstadoPedido.EN_COCINA);
            given(pedidoRepository.findByMesaIdAndEstadoIn(1L, estadosActivos)).willReturn(Optional.of(pedido));
            given(pedidoMapper.toResponseDTO(pedido)).willReturn(pedidoResponseDTO);

            // When
            PedidoResponseDTO resultado = pedidoService.obtenerPedidoActivoPorMesa(1L);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(pedidoResponseDTO);
        }

        @Test
        @DisplayName("Debe calcular el total de ventas del día correctamente")
        void calcularTotalVentasDelDia_Exito() {
            // Given
            BigDecimal totalEsperado = new BigDecimal("1500.50");
            given(pedidoRepository.sumarTotalVentasPorPeriodo(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(totalEsperado);

            // When
            BigDecimal resultado = pedidoService.calcularTotalVentasDelDia();

            // Then
            assertThat(resultado).isEqualTo(totalEsperado);
        }

        @Test
        @DisplayName("Debe retornar BigDecimal.ZERO si no hay ventas en el día")
        void calcularTotalVentasDelDia_SinVentas_RetornaCero() {
            // Given
            given(pedidoRepository.sumarTotalVentasPorPeriodo(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(null);

            // When
            BigDecimal resultado = pedidoService.calcularTotalVentasDelDia();

            // Then
            assertThat(resultado).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debe buscar pedidos por cliente de forma paginada")
        void obtenerHistorialClientePaginado_Exito() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Pedido> pagePedido = new PageImpl<>(List.of(pedido));

            given(pedidoRepository.findByNombreClienteAndEstado("Juan Pérez", EstadoPedido.PENDIENTE, pageable))
                    .willReturn(pagePedido);
            given(pedidoMapper.toResponseDTO(pedido)).willReturn(pedidoResponseDTO);

            // When
            Page<PedidoResponseDTO> resultado = pedidoService.obtenerHistorialClientePaginado("Juan Pérez", "PENDIENTE", pageable);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1).contains(pedidoResponseDTO);
        }
    }

    // =========================================================================
    // 5. PRUEBAS PARA: cancelarPedido()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para cancelarPedido")
    class CancelarPedidoTests {

        @Test
        @DisplayName("Debe cancelar el pedido y liberar la mesa vinculada dejándola LIBRE")
        void cancelarPedido_ExitoConMesa() {
            // Given
            pedido.setMesa(mesa);
            given(pedidoRepository.findById(10L)).willReturn(Optional.of(pedido));

            // When
            pedidoService.cancelarPedido(10L);

            // Then
            assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
            assertThat(mesa.getEstado()).isEqualTo(EstadoMesa.LIBRE);

            then(mesaRepository).should(times(1)).save(mesa);
            then(pedidoRepository).should(times(1)).save(pedido);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el pedido a cancelar no existe")
        void cancelarPedido_NoExiste_LanzaExcepcion() {
            // Given
            given(pedidoRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> pedidoService.cancelarPedido(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No se puede cancelar: Pedido no encontrado");

            then(pedidoRepository).should(never()).save(any());
        }
    }
}
