package com.Sistema.Backend.Reportes.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// Para la gráfica de líneas (Ventas diarias)
@Getter
@Setter
@AllArgsConstructor
public class VentasPorPeriodoDTO {

    private String etiquetaPeriodo; // Ej: "2026-05-29" o "Lunes"
    private BigDecimal totalVentas;
    private long cantidadPedidos;
}
