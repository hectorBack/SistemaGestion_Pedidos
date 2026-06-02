package com.Sistema.Backend.Reportes.Services.Impl;

import com.Sistema.Backend.Reportes.Dto.ReporteVentasDTO;
import com.Sistema.Backend.Pedidos.Repository.DetallePedidoRepository;
import com.Sistema.Backend.Pedidos.Repository.PedidoRepository;
import com.Sistema.Backend.Reportes.Dto.VentasPorCategoriaDTO;
import com.Sistema.Backend.Reportes.Dto.VentasPorPeriodoDTO;
import com.Sistema.Backend.Reportes.Services.ReporteService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detalleRepository;

    public ReporteServiceImpl(PedidoRepository pedidoRepository, DetallePedidoRepository detalleRepository) {
        this.pedidoRepository = pedidoRepository;
        this.detalleRepository = detalleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteVentasDTO generarResumenVentas(LocalDateTime inicio, LocalDateTime fin) {
        // 1. Obtener KPIs Básicos de Pedidos
        long pedidosExitosos = pedidoRepository.contarPedidosExitosos(inicio, fin);
        long pedidosCancelados = pedidoRepository.contarPedidosCancelados(inicio, fin);

        BigDecimal ingresosRaw = pedidoRepository.sumarTotalVentasPorPeriodo(inicio, fin);
        BigDecimal ingresosTotales = ingresosRaw != null ? ingresosRaw : BigDecimal.ZERO;

        // 2. Calcular Ticket Promedio de forma segura (Evitar división por cero)
        BigDecimal ticketPromedio = BigDecimal.ZERO;
        if (pedidosExitosos > 0) {
            ticketPromedio = ingresosTotales.divide(BigDecimal.valueOf(pedidosExitosos), 2, RoundingMode.HALF_UP);
        }

        // 3. Obtener KPIs de Productos
        Long totalProductosRaw = detalleRepository.contarTotalProductosVendidos(inicio, fin);
        long totalProductosVendidos = totalProductosRaw != null ? totalProductosRaw : 0;

        // 4. Obtener listas complejas agrupadas para las gráficas de Vue
        // Extraemos un Top 5 de productos más vendidos usando PageRequest
        List<String> topProductos = detalleRepository.encontrarProductosMasVendidos(inicio, fin, PageRequest.of(0, 5));

        // 1. Invocar la nueva consulta nativa del repositorio
        List<Object[]> resultadosCategoriasRaw = detalleRepository.obtenerVentasPorCategoriaNativo(inicio, fin);

        // 2. Mapear de forma segura el Object[] a tu clase VentasPorCategoriaDTO
        List<VentasPorCategoriaDTO> ventasPorCategoria = resultadosCategoriasRaw.stream().map(row -> {
            String categoria = (String) row[0];
            // Postgres devuelve BigInteger o Long en los SUM de cantidades enteras
            long cantidadVendida = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            BigDecimal totalRecaudado = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

            return new VentasPorCategoriaDTO(categoria, cantidadVendida, totalRecaudado);
        }).collect(Collectors.toList());


        // 1. Llamamos a la query nativa
        List<Object[]> resultadosDiariosRaw = pedidoRepository.obtenerVentasDiariasNativo(inicio, fin);

// 2. Mapeamos manualmente el Object[] a tu DTO de soporte de forma limpia
        List<VentasPorPeriodoDTO> ventasCronologicas = resultadosDiariosRaw.stream().map(row -> {
            String etiqueta = (String) row[0];
            BigDecimal total = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            // Capturamos el conteo (Postgres suele devolver BigInteger o Long en COUNT nativos)
            long cantidad = row[2] != null ? ((Number) row[2]).longValue() : 0L;

            return new VentasPorPeriodoDTO(etiqueta, total, cantidad);
        }).collect(Collectors.toList());

        // 5. Construir y retornar el DTO integrado
        return new ReporteVentasDTO(
                inicio,
                fin,
                pedidosExitosos,
                pedidosCancelados,
                ingresosTotales,
                ticketPromedio,
                totalProductosVendidos,
                topProductos,
                ventasPorCategoria,
                ventasCronologicas
        );
    }
}
