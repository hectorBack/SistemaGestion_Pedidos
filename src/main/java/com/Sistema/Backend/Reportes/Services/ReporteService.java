package com.Sistema.Backend.Reportes.Services;

import com.Sistema.Backend.Reportes.Dto.ReporteVentasDTO;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

public interface ReporteService {

    ReporteVentasDTO generarResumenVentas(LocalDateTime inicio, LocalDateTime fin);

    // Métodos de exportación
    ByteArrayInputStream exportarReporteExcel(LocalDateTime inicio, LocalDateTime fin);
    ByteArrayInputStream exportarReportePDF(LocalDateTime inicio, LocalDateTime fin);
}
