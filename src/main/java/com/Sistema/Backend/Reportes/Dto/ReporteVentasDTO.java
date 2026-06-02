package com.Sistema.Backend.Reportes.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ReporteVentasDTO {
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    // KPIs Básicos
    // KPIs Básicos
    private long totalPedidosExitosos;
    private long totalPedidosCancelados;
    private BigDecimal ingresosTotales;
    private BigDecimal ticketPromedio; // 🌟 Nuevo KPI
    private long totalProductosVendidos;

    // Listas para Gráficas en Vue
    private List<String> topProductos; // 🌟 Cambiar a Top 5 o Top 10 en lugar de solo 1
    private List<VentasPorCategoriaDTO> ventasPorCategoria; // 🌟 Para gráfica de pastel
    private List<VentasPorPeriodoDTO> ventasCronologicas;   // 🌟 Para gráfica de líneas

}
