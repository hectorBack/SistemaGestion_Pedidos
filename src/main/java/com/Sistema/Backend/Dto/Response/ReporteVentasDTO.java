package com.Sistema.Backend.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReporteVentasDTO {

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private long totalPedidos;
    private BigDecimal ingresosTotales;
    private long totalProductosVendidos;
    private String productoMasVendido;
}
