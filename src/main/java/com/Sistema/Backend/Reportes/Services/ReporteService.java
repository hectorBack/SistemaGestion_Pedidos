package com.Sistema.Backend.Reportes.Services;

import com.Sistema.Backend.Reportes.Dto.ReporteVentasDTO;

import java.time.LocalDateTime;

public interface ReporteService {

    ReporteVentasDTO generarResumenVentas(LocalDateTime inicio, LocalDateTime fin);
}
