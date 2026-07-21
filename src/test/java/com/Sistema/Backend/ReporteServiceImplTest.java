package com.Sistema.Backend;

import com.Sistema.Backend.Exception.BadRequestException;
import com.Sistema.Backend.Pedidos.Repository.DetallePedidoRepository;
import com.Sistema.Backend.Pedidos.Repository.PedidoRepository;
import com.Sistema.Backend.Reportes.Dto.ReporteVentasDTO;
import com.Sistema.Backend.Reportes.Dto.VentasPorCategoriaDTO;
import com.Sistema.Backend.Reportes.Dto.VentasPorPeriodoDTO;
import com.Sistema.Backend.Reportes.Services.Impl.ReporteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ReporteServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DetallePedidoRepository detalleRepository;

    @InjectMocks
    private ReporteServiceImpl reporteService;

    private LocalDateTime inicio;
    private LocalDateTime fin;

    @BeforeEach
    void setUp() {
        inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        fin = LocalDateTime.of(2026, 1, 31, 23, 59);
    }

    // =========================================================================
    // 1. PRUEBAS PARA: generarResumenVentas()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para generación del Resumen de Ventas (KPIs y Lógica Financiera)")
    class GenerarResumenVentasTests {

        @Test
        @DisplayName("Debe generar exitosamente el reporte con métricas completas y cálculos financieros correctos")
        void generarResumenVentas_Exito() {
            // Given
            given(pedidoRepository.contarPedidosExitosos(inicio, fin)).willReturn(10L);
            given(pedidoRepository.contarPedidosCancelados(inicio, fin)).willReturn(2L);
            given(detalleRepository.contarTotalProductosVendidos(inicio, fin)).willReturn(40L);
            given(pedidoRepository.sumarTotalVentasPorPeriodo(inicio, fin)).willReturn(new BigDecimal("1500.00"));

            List<String> topProductosMock = List.of("Tacos al Pastor", "Hamburguesa");
            given(detalleRepository.encontrarProductosMasVendidos(any(), any(), any(Pageable.class)))
                    .willReturn(topProductosMock);

            // Mock de ventas por categoría (Object[] { String categoria, Number cantidad, BigDecimal total })
            Object[] catRow1 = new Object[]{"Bebidas", 15L, new BigDecimal("450.00")};
            given(detalleRepository.obtenerVentasPorCategoriaNativo(inicio, fin))
                    .willReturn(List.<Object[]>of(catRow1));

            // Mock de ventas cronológicas (Object[] { String fecha, BigDecimal total, Number cantidad })
            Object[] cronoRow1 = new Object[]{"2026-01-15", new BigDecimal("500.00"), 5L};
            given(pedidoRepository.obtenerVentasDiariasNativo(inicio, fin))
                    .willReturn(List.<Object[]>of(cronoRow1));

            // When
            ReporteVentasDTO dto = reporteService.generarResumenVentas(inicio, fin);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.getFechaInicio()).isEqualTo(inicio);
            assertThat(dto.getFechaFin()).isEqualTo(fin);
            assertThat(dto.getTotalPedidosExitosos()).isEqualTo(10L);
            assertThat(dto.getTotalPedidosCancelados()).isEqualTo(2L);
            assertThat(dto.getTotalProductosVendidos()).isEqualTo(40L);

            // Verificación del cálculo del Ticket Promedio: 1500.00 / 10 = 150.00
            assertThat(dto.getIngresosTotales()).isEqualByComparingTo("1500.00");
            assertThat(dto.getTicketPromedio()).isEqualByComparingTo("150.00");

            // Mapeos nativos
            assertThat(dto.getTopProductos()).containsExactly("Tacos al Pastor", "Hamburguesa");
            assertThat(dto.getVentasPorCategoria()).hasSize(1);
            assertThat(dto.getVentasPorCategoria().get(0).getCategoria()).isEqualTo("Bebidas");
            assertThat(dto.getVentasCronologicas()).hasSize(1);
            assertThat(dto.getVentasCronologicas().get(0).getEtiquetaPeriodo()).isEqualTo("2026-01-15");
        }

        @Test
        @DisplayName("Debe manejar correctamente valores nulos desde los repositorios y evitar NullPointerException")
        void generarResumenVentas_ValoresNulos_ManejaDefaults() {
            // Given
            given(pedidoRepository.contarPedidosExitosos(inicio, fin)).willReturn(0L);
            given(pedidoRepository.contarPedidosCancelados(inicio, fin)).willReturn(0L);
            given(detalleRepository.contarTotalProductosVendidos(inicio, fin)).willReturn(null);
            given(pedidoRepository.sumarTotalVentasPorPeriodo(inicio, fin)).willReturn(null);
            given(detalleRepository.encontrarProductosMasVendidos(any(), any(), any(Pageable.class)))
                    .willReturn(Collections.emptyList());

            // Fila nativa con nulos
            Object[] catRowNull = new Object[]{"Postres", null, null};
            given(detalleRepository.obtenerVentasPorCategoriaNativo(inicio, fin))
                    .willReturn(List.<Object[]>of(catRowNull));

            Object[] cronoRowNull = new Object[]{"2026-01-01", null, null};
            given(pedidoRepository.obtenerVentasDiariasNativo(inicio, fin))
                    .willReturn(List.<Object[]>of(cronoRowNull));

            // When
            ReporteVentasDTO dto = reporteService.generarResumenVentas(inicio, fin);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.getIngresosTotales()).isEqualTo(BigDecimal.ZERO);
            assertThat(dto.getTicketPromedio()).isEqualTo(BigDecimal.ZERO);
            assertThat(dto.getTotalProductosVendidos()).isEqualTo(0L);

            VentasPorCategoriaDTO catDto = dto.getVentasPorCategoria().get(0);
            assertThat(catDto.getCantidadVendida()).isEqualTo(0L);
            assertThat(catDto.getTotalRecaudado()).isEqualTo(BigDecimal.ZERO);

            VentasPorPeriodoDTO cronoDto = dto.getVentasCronologicas().get(0);
            assertThat(cronoDto.getTotalVentas()).isEqualTo(BigDecimal.ZERO);
            assertThat(cronoDto.getCantidadPedidos()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la fecha fin es anterior a la fecha inicio")
        void generarResumenVentas_RangoFechasInvalido_LanzaExcepcion() {
            // Given
            LocalDateTime fechaInvalidaFin = inicio.minusDays(1);

            // When / Then
            assertThatThrownBy(() -> reporteService.generarResumenVentas(inicio, fechaInvalidaFin))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El rango de fechas es inválido. La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
    }

    // =========================================================================
    // 2. PRUEBAS PARA: exportarReporteExcel()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para exportación a Excel (.xlsx)")
    class ExportarReporteExcelTests {

        @Test
        @DisplayName("Debe generar el flujo de bytes de Excel (.xlsx) correctamente")
        void exportarReporteExcel_Exito() {
            // Given
            given(pedidoRepository.contarPedidosExitosos(inicio, fin)).willReturn(5L);
            given(pedidoRepository.contarPedidosCancelados(inicio, fin)).willReturn(1L);
            given(detalleRepository.contarTotalProductosVendidos(inicio, fin)).willReturn(10L);
            given(pedidoRepository.sumarTotalVentasPorPeriodo(inicio, fin)).willReturn(new BigDecimal("500.00"));
            given(detalleRepository.encontrarProductosMasVendidos(any(), any(), any(Pageable.class)))
                    .willReturn(Collections.emptyList());

            // When
            ByteArrayInputStream stream = reporteService.exportarReporteExcel(inicio, fin);

            // Then
            assertThat(stream).isNotNull();
            assertThat(stream.available()).isGreaterThan(0);
        }
    }

    // =========================================================================
    // 3. PRUEBAS PARA: exportarReportePDF()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para exportación a PDF")
    class ExportarReportePDFTests {

        @Test
        @DisplayName("Debe generar el flujo de bytes de PDF correctamente")
        void exportarReportePDF_Exito() {
            // Given
            given(pedidoRepository.contarPedidosExitosos(inicio, fin)).willReturn(8L);
            given(pedidoRepository.contarPedidosCancelados(inicio, fin)).willReturn(2L);
            given(detalleRepository.contarTotalProductosVendidos(inicio, fin)).willReturn(25L);
            given(pedidoRepository.sumarTotalVentasPorPeriodo(inicio, fin)).willReturn(new BigDecimal("1200.00"));
            given(detalleRepository.encontrarProductosMasVendidos(any(), any(), any(Pageable.class)))
                    .willReturn(Collections.emptyList());

            // When
            ByteArrayInputStream stream = reporteService.exportarReportePDF(inicio, fin);

            // Then
            assertThat(stream).isNotNull();
            assertThat(stream.available()).isGreaterThan(0);
        }
    }
}
