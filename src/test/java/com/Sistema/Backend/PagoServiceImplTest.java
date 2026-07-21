package com.Sistema.Backend;

import com.Sistema.Backend.Exception.BadRequestException;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Pagos.Dto.ReembolsoRequestDTO;
import com.Sistema.Backend.Pagos.Dto.Request.PagoRequestDTO;
import com.Sistema.Backend.Pagos.Dto.Response.HistorialPagosResponseDTO;
import com.Sistema.Backend.Pagos.Dto.Response.PagoResponseDTO;
import com.Sistema.Backend.Pagos.Entity.EstadoPago;
import com.Sistema.Backend.Pagos.Entity.MetodoPago;
import com.Sistema.Backend.Pagos.Entity.Pago;
import com.Sistema.Backend.Pagos.Mapper.PagoMapper;
import com.Sistema.Backend.Pagos.Repository.PagoRepository;
import com.Sistema.Backend.Pagos.Services.Impl.PagoServiceImpl;
import com.Sistema.Backend.Pedidos.Entity.EstadoPedido;
import com.Sistema.Backend.Pedidos.Entity.Pedido;
import com.Sistema.Backend.Pedidos.Repository.PedidoRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class PagoServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PagoMapper pagoMapper;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private Pedido pedido;
    private Pago pago;
    private PagoRequestDTO pagoRequestDTO;
    private PagoResponseDTO pagoResponseDTO;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setId(10L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setNotas("Nota inicial");

        pago = new Pago();
        pago.setId(1L);
        pago.setCodigoTransaccion("TX-123456");
        pago.setMetodoPago(MetodoPago.EFECTIVO);
        pago.setEstado(EstadoPago.APROBADO);
        pago.setMonto(new BigDecimal("150.00"));
        pago.setPedido(pedido);

        pagoRequestDTO = new PagoRequestDTO();
        pagoRequestDTO.setPedidoId(10L);
        pagoRequestDTO.setMetodoPago(MetodoPago.EFECTIVO.name());
        pagoRequestDTO.setMonto(new BigDecimal("150.00"));

        pagoResponseDTO = new PagoResponseDTO();
        pagoResponseDTO.setId(1L);
        pagoResponseDTO.setCodigoTransaccion("TX-123456");
        pagoResponseDTO.setEstado(EstadoPago.APROBADO.name());
        pagoResponseDTO.setMonto(new BigDecimal("150.00"));
    }

    // =========================================================================
    // 1. PRUEBAS PARA: registrarPago()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para registrarPago")
    class RegistrarPagoTests {

        @Test
        @DisplayName("Debe registrar el pago y cambiar el estado del pedido a EN_COCINA cuando se aprueba")
        void registrarPago_Exito_AvanzaPedidoAEnCocina() {
            // Given
            given(pedidoRepository.findById(10L)).willReturn(Optional.of(pedido));
            given(pagoMapper.toEntity(pagoRequestDTO)).willReturn(pago);
            given(pagoRepository.save(pago)).willReturn(pago);
            given(pedidoRepository.save(pedido)).willReturn(pedido);
            given(pagoMapper.toResponseDTO(pago)).willReturn(pagoResponseDTO);

            // When
            PagoResponseDTO resultado = pagoService.registrarPago(pagoRequestDTO);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(pagoResponseDTO);
            assertThat(pago.getEstado()).isEqualTo(EstadoPago.APROBADO);
            assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.EN_COCINA);

            then(pedidoRepository).should(times(1)).findById(10L);
            then(pagoRepository).should(times(1)).save(pago);
            then(pedidoRepository).should(times(1)).save(pedido);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el pedido asociado no existe")
        void registrarPago_PedidoNoExiste_LanzaExcepcion() {
            // Given
            given(pedidoRepository.findById(10L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> pagoService.registrarPago(pagoRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Pedido no encontrado con ID: 10");

            then(pagoRepository).should(never()).save(any());
            then(pedidoRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 2. PRUEBAS PARA: obtenerPorCodigo()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerPorCodigo")
    class ObtenerPorCodigoTests {

        @Test
        @DisplayName("Debe retornar el DTO del pago al encontrar la transacción por su código público")
        void obtenerPorCodigo_Exito() {
            // Given
            given(pagoRepository.findByCodigoTransaccion("TX-123456")).willReturn(Optional.of(pago));
            given(pagoMapper.toResponseDTO(pago)).willReturn(pagoResponseDTO);

            // When
            PagoResponseDTO resultado = pagoService.obtenerPorCodigo("TX-123456");

            // Then
            assertThat(resultado).isNotNull().isEqualTo(pagoResponseDTO);
            then(pagoRepository).should(times(1)).findByCodigoTransaccion("TX-123456");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el código de transacción no existe")
        void obtenerPorCodigo_NoExiste_LanzaExcepcion() {
            // Given
            given(pagoRepository.findByCodigoTransaccion("INVALIDO")).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> pagoService.obtenerPorCodigo("INVALIDO"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Transacción de pago no encontrada: INVALIDO");
        }
    }

    // =========================================================================
    // 3. PRUEBAS PARA: obtenerPorPedidoId()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerPorPedidoId")
    class ObtenerPorPedidoIdTests {

        @Test
        @DisplayName("Debe retornar el DTO del pago cuando está vinculado al pedido solicitado")
        void obtenerPorPedidoId_Exito() {
            // Given
            given(pagoRepository.findByPedidoId(10L)).willReturn(Optional.of(pago));
            given(pagoMapper.toResponseDTO(pago)).willReturn(pagoResponseDTO);

            // When
            PagoResponseDTO resultado = pagoService.obtenerPorPedidoId(10L);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(pagoResponseDTO);
            then(pagoRepository).should(times(1)).findByPedidoId(10L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si no hay ningún pago asociado al Pedido ID")
        void obtenerPorPedidoId_NoExiste_LanzaExcepcion() {
            // Given
            given(pagoRepository.findByPedidoId(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> pagoService.obtenerPorPedidoId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No se encontró ningún pago asociado al pedido con ID: 99");
        }
    }

    // =========================================================================
    // 4. PRUEBAS PARA: obtenerTodos()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerTodos")
    class ObtenerTodosTests {

        @Test
        @DisplayName("Debe retornar la lista completa e histórica de pagos mapeados")
        void obtenerTodos_Exito() {
            // Given
            given(pagoRepository.findAll()).willReturn(List.of(pago));
            given(pagoMapper.toResponseDTO(pago)).willReturn(pagoResponseDTO);

            // When
            List<PagoResponseDTO> resultado = pagoService.obtenerTodos();

            // Then
            assertThat(resultado).isNotNull().hasSize(1).contains(pagoResponseDTO);
            then(pagoRepository).should(times(1)).findAll();
        }
    }

    // =========================================================================
    // 5. PRUEBAS PARA: obtenerPagosFiltrados()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerPagosFiltrados")
    class ObtenerPagosFiltradosTests {

        @Test
        @DisplayName("Debe filtrar pagos calculando el rango exacto de fechas y la sumatoria acumulada")
        void obtenerPagosFiltrados_Exito() {
            // Given
            LocalDate inicio = LocalDate.of(2026, 3, 1);
            LocalDate fin = LocalDate.of(2026, 3, 10);
            LocalDateTime fechaInicioExpected = inicio.atStartOfDay();
            LocalDateTime fechaFinExpected = fin.atTime(LocalTime.MAX);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Pago> pagePago = new PageImpl<>(List.of(pago));
            BigDecimal totalAcumulado = new BigDecimal("150.00");
            HistorialPagosResponseDTO historialExpected = new HistorialPagosResponseDTO();

            given(pagoRepository.filtrarPagos(eq(MetodoPago.EFECTIVO), eq(fechaInicioExpected), eq(fechaFinExpected), eq(pageable)))
                    .willReturn(pagePago);
            given(pagoMapper.toResponseDTO(pago)).willReturn(pagoResponseDTO);
            given(pagoRepository.sumarTotalPorFiltros(eq(MetodoPago.EFECTIVO), eq(fechaInicioExpected), eq(fechaFinExpected)))
                    .willReturn(totalAcumulado);
            given(pagoMapper.toHistorialResponseDTO(any(), eq(totalAcumulado))).willReturn(historialExpected);

            // When
            HistorialPagosResponseDTO resultado = pagoService.obtenerPagosFiltrados(MetodoPago.EFECTIVO, inicio, fin, pageable);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(historialExpected);

            then(pagoRepository).should(times(1)).filtrarPagos(MetodoPago.EFECTIVO, fechaInicioExpected, fechaFinExpected, pageable);
            then(pagoRepository).should(times(1)).sumarTotalPorFiltros(MetodoPago.EFECTIVO, fechaInicioExpected, fechaFinExpected);
            then(pagoMapper).should(times(1)).toHistorialResponseDTO(any(), eq(totalAcumulado));
        }
    }

    // =========================================================================
    // 6. PRUEBAS PARA: reembolsarPago()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para reembolsarPago")
    class ReembolsarPagoTests {

        private ReembolsoRequestDTO reembolsoRequest;

        @BeforeEach
        void setUpReembolso() {
            reembolsoRequest = new ReembolsoRequestDTO();
            reembolsoRequest.setMotivo("Error en la orden");
        }

        @Test
        @DisplayName("Debe cambiar el estado del pago a REEMBOLSADO y cancelar el pedido automáticamente")
        void reembolsarPago_Exito() {
            // Given
            pago.setNotas("Pago en efectivo en caja");
            given(pagoRepository.findById(1L)).willReturn(Optional.of(pago));
            given(pagoRepository.save(pago)).willReturn(pago);
            given(pedidoRepository.save(pedido)).willReturn(pedido);
            given(pagoMapper.toResponseDTO(pago)).willReturn(pagoResponseDTO);

            // When
            PagoResponseDTO resultado = pagoService.reembolsarPago(1L, reembolsoRequest);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(pago.getEstado()).isEqualTo(EstadoPago.REEMBOLSADO);
            assertThat(pago.getNotas()).contains("REEMBOLSO: Error en la orden");

            // Verificación del pedido cancelado
            assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
            assertThat(pedido.getNotas()).contains("Pedido cancelado automáticamente por reembolso de caja. Motivo: Error en la orden");

            then(pagoRepository).should(times(1)).findById(1L);
            then(pagoRepository).should(times(1)).save(pago);
            then(pedidoRepository).should(times(1)).save(pedido);
        }

        @Test
        @DisplayName("Debe reembolsar correctamente incluso si el pago no tiene notas previas (notas == null)")
        void reembolsarPago_SinNotasPrevias_Exito() {
            // Given
            pago.setNotas(null);
            given(pagoRepository.findById(1L)).willReturn(Optional.of(pago));
            given(pagoRepository.save(pago)).willReturn(pago);
            given(pedidoRepository.save(pedido)).willReturn(pedido);
            given(pagoMapper.toResponseDTO(pago)).willReturn(pagoResponseDTO);

            // When
            pagoService.reembolsarPago(1L, reembolsoRequest);

            // Then
            assertThat(pago.getNotas()).isEqualTo("REEMBOLSO: Error en la orden");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el pago a reembolsar no existe")
        void reembolsarPago_PagoNoExiste_LanzaExcepcion() {
            // Given
            given(pagoRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> pagoService.reembolsarPago(99L, reembolsoRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No se encontró el pago con ID: 99");

            then(pagoRepository).should(never()).save(any());
            then(pedidoRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la transacción no está en estado APROBADO")
        void reembolsarPago_EstadoNoAprobado_LanzaExcepcion() {
            // Given
            pago.setEstado(EstadoPago.REEMBOLSADO);
            given(pagoRepository.findById(1L)).willReturn(Optional.of(pago));

            // When / Then
            assertThatThrownBy(() -> pagoService.reembolsarPago(1L, reembolsoRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Solo se pueden reembolsar transacciones con estado APROBADO");

            then(pagoRepository).should(never()).save(any());
            then(pedidoRepository).should(never()).save(any());
        }
    }
}
