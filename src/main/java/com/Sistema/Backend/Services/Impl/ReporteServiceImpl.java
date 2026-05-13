package com.Sistema.Backend.Services.Impl;

import com.Sistema.Backend.Dto.Response.ReporteVentasDTO;
import com.Sistema.Backend.Repository.DetallePedidoRepository;
import com.Sistema.Backend.Repository.PedidoRepository;
import com.Sistema.Backend.Services.ReporteService;
import org.hibernate.annotations.SecondaryRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detalleRepository;

    public ReporteServiceImpl(PedidoRepository pedidoRepository, DetallePedidoRepository detalleRepository) {
        this.pedidoRepository = pedidoRepository;
        this.detalleRepository = detalleRepository;
    }

    @Override
    public ReporteVentasDTO generarResumenVentas(LocalDateTime inicio, LocalDateTime fin) {
        // 1. Datos básicos del pedido
        BigDecimal ingresos = pedidoRepository.sumarTotalVentasPorPeriodo(inicio, fin);
        long totalPedidos = pedidoRepository.countByFechaCreacionBetween(inicio, fin);

        // 2. Datos avanzados del detalle
        Long productosVendidos = detalleRepository.contarTotalProductosVendidos(inicio, fin);

        // Obtenemos el top 1 producto (usando PageRequest para limitar a 1 resultado)
        List<String> topProductos = detalleRepository.encontrarProductoMasVendido(
                inicio, fin, org.springframework.data.domain.PageRequest.of(0, 1));

        String masVendido = topProductos.isEmpty() ? "N/A" : topProductos.get(0);

        return new ReporteVentasDTO(
                inicio,
                fin,
                totalPedidos,
                ingresos != null ? ingresos : BigDecimal.ZERO,
                productosVendidos != null ? productosVendidos : 0,
                masVendido
        );
    }
}
