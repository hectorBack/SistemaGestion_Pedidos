package com.Sistema.Backend.Services.Impl;

import com.Sistema.Backend.Dto.Response.ReporteVentasDTO;
import com.Sistema.Backend.Repository.PedidoRepository;
import com.Sistema.Backend.Services.ReporteService;
import org.hibernate.annotations.SecondaryRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final PedidoRepository pedidoRepository;

    public ReporteServiceImpl(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public ReporteVentasDTO generarResumenVentas(LocalDateTime inicio, LocalDateTime fin) {
        // 1. Obtener ingresos totales y conteo de pedidos en el rango
        // Aprovechamos los métodos que ya tienes o creamos uno genérico
        BigDecimal ingresos = pedidoRepository.sumarTotalVentasPorPeriodo(inicio, fin);
        long conteo = pedidoRepository.countByFechaCreacionBetween(inicio, fin);

        // 2. Lógica para el producto más vendido (Opcional: puedes dejarlo en cero por ahora)
        // O podrías hacer una query personalizada en el futuro

        return new ReporteVentasDTO(
                inicio,
                fin,
                conteo,
                ingresos != null ? ingresos : BigDecimal.ZERO,
                0, // Aquí iría la lógica de ítems totales
                "Consultar detalle en pedidos"
        );
    }
}
