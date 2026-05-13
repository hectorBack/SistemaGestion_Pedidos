package com.Sistema.Backend.Services;

import com.Sistema.Backend.Dto.Response.ReporteVentasDTO;

import java.time.LocalDateTime;

public interface ReporteService {

    ReporteVentasDTO generarResumenVentas(LocalDateTime inicio, LocalDateTime fin);
}
