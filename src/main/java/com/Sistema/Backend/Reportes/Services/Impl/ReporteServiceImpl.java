package com.Sistema.Backend.Reportes.Services.Impl;

import com.Sistema.Backend.Exception.BadRequestException;
import com.Sistema.Backend.Exception.InternalServerException;
import com.Sistema.Backend.Reportes.Dto.ReporteVentasDTO;
import com.Sistema.Backend.Pedidos.Repository.DetallePedidoRepository;
import com.Sistema.Backend.Pedidos.Repository.PedidoRepository;
import com.Sistema.Backend.Reportes.Dto.VentasPorCategoriaDTO;
import com.Sistema.Backend.Reportes.Dto.VentasPorPeriodoDTO;
import com.Sistema.Backend.Reportes.Services.ReporteService;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
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
        log.info("Iniciando generación de resumen de ventas de: {} a: {}", inicio, fin);

        // VALIDACIÓN DE NEGOCIO: Rango temporal consistente
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            log.error("Fallo al generar reporte: La fecha final {} es anterior a la inicial {}", fin, inicio);
            throw new BadRequestException("El rango de fechas es inválido. La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        // 1. Obtener contadores operativos base
        long pedidosExitosos = pedidoRepository.contarPedidosExitosos(inicio, fin);
        long pedidosCancelados = pedidoRepository.contarPedidosCancelados(inicio, fin);
        long totalProductosVendidos = obtenerTotalProductosVendidos(inicio, fin);

        log.debug("Métricas base recuperadas - Exitosos: {}, Cancelados: {}, Productos: {}",
                pedidosExitosos, pedidosCancelados, totalProductosVendidos);

        // 2. Obtener y calcular métricas financieras
        BigDecimal ingresosTotales = obtenerIngresosTotales(inicio, fin);
        BigDecimal ticketPromedio = calcularTicketPromedio(ingresosTotales, pedidosExitosos);

        // 3. Obtener sets de datos analíticos para componentes visuales
        List<String> topProductos = detalleRepository.encontrarProductosMasVendidos(inicio, fin, PageRequest.of(0, 5));
        List<VentasPorCategoriaDTO> ventasPorCategoria = obtenerMapeoVentasPorCategoria(inicio, fin);
        List<VentasPorPeriodoDTO> ventasCronologicas = obtenerMapeoVentasCronologicas(inicio, fin);

        log.info("Estructura de reporte consolidada con éxito para el periodo solicitado");

        // 4. Construir y retornar la carga útil (Payload) estructurada
        return new ReporteVentasDTO(
                inicio, fin, pedidosExitosos, pedidosCancelados, ingresosTotales,
                ticketPromedio, totalProductosVendidos, topProductos, ventasPorCategoria, ventasCronologicas
        );
    }

    //Métodos privados para el metodo generarResumenVentas

    private BigDecimal obtenerIngresosTotales(LocalDateTime inicio, LocalDateTime fin) {
        BigDecimal ingresosRaw = pedidoRepository.sumarTotalVentasPorPeriodo(inicio, fin);
        return ingresosRaw != null ? ingresosRaw : BigDecimal.ZERO;
    }

    private BigDecimal calcularTicketPromedio(BigDecimal ingresosTotales, long pedidosExitosos) {
        if (pedidosExitosos <= 0) {
            return BigDecimal.ZERO;
        }
        return ingresosTotales.divide(BigDecimal.valueOf(pedidosExitosos), 2, RoundingMode.HALF_UP);
    }

    private long obtenerTotalProductosVendidos(LocalDateTime inicio, LocalDateTime fin) {
        Long totalProductosRaw = detalleRepository.contarTotalProductosVendidos(inicio, fin);
        return totalProductosRaw != null ? totalProductosRaw : 0L;
    }

    private List<VentasPorCategoriaDTO> obtenerMapeoVentasPorCategoria(LocalDateTime inicio, LocalDateTime fin) {
        return detalleRepository.obtenerVentasPorCategoriaNativo(inicio, fin).stream()
                .map(row -> new VentasPorCategoriaDTO(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).longValue() : 0L,
                        row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO
                ))
                .collect(Collectors.toList());
    }

    private List<VentasPorPeriodoDTO> obtenerMapeoVentasCronologicas(LocalDateTime inicio, LocalDateTime fin) {
        return pedidoRepository.obtenerVentasDiariasNativo(inicio, fin).stream()
                .map(row -> new VentasPorPeriodoDTO(
                        (String) row[0],
                        row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO,
                        row[2] != null ? ((Number) row[2]).longValue() : 0L
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportarReporteExcel(LocalDateTime inicio, LocalDateTime fin) {
        log.info("Solicitud para exportar Reporte Ejecutivo a formato MS Excel (.xlsx)");
        ReporteVentasDTO datos = generarResumenVentas(inicio, fin);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Resumen de Ventas");

            // 1. Inicializar Estilos Estructurados
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle moneyStyle = crearEstiloMoneda(workbook);

            // 2. Construcción de Encabezados y Títulos
            crearFilaTexto(sheet, 0, "REPORTE ANALÍTICO DE VENTAS");
            crearFilaTexto(sheet, 1, "Periodo: " + inicio.format(formatter) + " al " + fin.format(formatter));

            // 3. Tabla de KPIs - Encabezados
            Row tableHeader = sheet.createRow(3);
            crearCelda(tableHeader, 0, "Métrica Operativa", headerStyle);
            crearCelda(tableHeader, 1, "Valor Registrado", headerStyle);

            // 4. Inyección de datos iterativos utilizando abstracción
            Object[][] kpiDatos = {
                    {"Ingresos Totales", datos.getIngresosTotales().doubleValue(), true},
                    {"Ticket Promedio", datos.getTicketPromedio().doubleValue(), true},
                    {"Pedidos Entregados", (double) datos.getTotalPedidosExitosos(), false},
                    {"Pedidos Cancelados", (double) datos.getTotalPedidosCancelados(), false},
                    {"Productos Vendidos", (double) datos.getTotalProductosVendidos(), false}
            };

            int rowIdx = 4;
            for (Object[] kpi : kpiDatos) {
                Row row = sheet.createRow(rowIdx++);
                crearCelda(row, 0, (String) kpi[0], null);

                boolean esMoneda = (boolean) kpi[2];
                crearCeldaNumerica(row, 1, (Double) kpi[1], esMoneda ? moneyStyle : null);
            }

            // 5. Ajustes de layout finales
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            log.info("Archivo Excel binario generado de forma correcta. Tamaño del stream: {} bytes", out.size());
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Error crítico durante la escritura o compilación del libro Excel POI", e);
            throw new InternalServerException("Error al generar el archivo Excel de reportes", e);
        }
    }

    // Métodos Helper Privados para Excel
    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }

    private void crearFilaTexto(Sheet sheet, int numFila, String texto) {
        Row row = sheet.createRow(numFila);
        row.createCell(0).setCellValue(texto);
    }

    private void crearCelda(Row row, int columna, String valor, CellStyle estilo) {
        Cell cell = row.createCell(columna);
        cell.setCellValue(valor);
        if (estilo != null) {
            cell.setCellStyle(estilo);
        }
    }

    private void crearCeldaNumerica(Row row, int columna, Double valor, CellStyle estilo) {
        Cell cell = row.createCell(columna);
        cell.setCellValue(valor);
        if (estilo != null) {
            cell.setCellStyle(estilo);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportarReportePDF(LocalDateTime inicio, LocalDateTime fin) {
        log.info("Solicitud para generar y exportar Reporte Ejecutivo a formato PDF");
        ReporteVentasDTO datos = generarResumenVentas(inicio, fin);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        java.text.NumberFormat formatMoneda = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "MX"));

        com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            com.lowagie.text.pdf.PdfWriter.getInstance(document, out);
            document.open();

            // 1. Inicializar Paleta de Colores y Fuentes Estáticas
            java.awt.Color azulCorporativo = new java.awt.Color(59, 130, 246);
            com.lowagie.text.Font titleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 20, java.awt.Color.BLACK);
            com.lowagie.text.Font subTitleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 11, java.awt.Color.GRAY);
            com.lowagie.text.Font labelFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 12, java.awt.Color.DARK_GRAY);
            com.lowagie.text.Font valueFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 12, java.awt.Color.BLACK);

            // 2. Encabezados del Documento
            document.add(crearParrafoCentrado("SISTEMA DE GESTIÓN DE PEDIDOS", titleFont));

            String subTexto = "Reporte Ejecutivo de Ventas\nPeriodo: " + inicio.format(formatter) + " a " + fin.format(formatter) + "\n\n";
            document.add(crearParrafoCentrado(subTexto, subTitleFont));

            // Separador Visual
            document.add(new com.lowagie.text.Paragraph("__________________________________________________________________\n\n"));

            // 3. Inicializar Tabla de KPIs
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Encabezados de la Tabla
            com.lowagie.text.Font headerFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 12, java.awt.Color.WHITE);
            table.addCell(crearCeldaEncabezado("Indicador Clave (KPI)", azulCorporativo, headerFont));
            table.addCell(crearCeldaEncabezado("Monto / Cantidad", azulCorporativo, headerFont));

            // 4. Llenado Dinámico de Datos
            addPdfRow(table, "Ingresos Totales", formatMoneda.format(datos.getIngresosTotales()), labelFont, valueFont);
            addPdfRow(table, "Ticket Promedio por Venta", formatMoneda.format(datos.getTicketPromedio()), labelFont, valueFont);
            addPdfRow(table, "Pedidos Entregados con Éxito", String.valueOf(datos.getTotalPedidosExitosos()), labelFont, valueFont);
            addPdfRow(table, "Pedidos Cancelados / Rechazados", String.valueOf(datos.getTotalPedidosCancelados()), labelFont, valueFont);
            addPdfRow(table, "Volumen Total de Productos Vendidos", String.valueOf(datos.getTotalProductosVendidos()), labelFont, valueFont);

            document.add(table);
            document.close();

            log.info("Documento PDF renderizado de forma óptima en el buffer de salida");
            return new ByteArrayInputStream(out.toByteArray());
        } catch (com.lowagie.text.DocumentException e) {
            log.error("Error grave en la manipulación estructural de iText/OpenPDF", e);
            throw new InternalServerException("Error construyendo el documento analítico en PDF", e);
        }
    }


    //Métodos Helper para PDF
    private com.lowagie.text.Paragraph crearParrafoCentrado(String texto, com.lowagie.text.Font fuente) {
        com.lowagie.text.Paragraph paragraph = new com.lowagie.text.Paragraph(new com.lowagie.text.Phrase(new com.lowagie.text.Chunk(texto, fuente)));
        paragraph.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        return paragraph;
    }

    private com.lowagie.text.pdf.PdfPCell crearCeldaEncabezado(String texto, java.awt.Color fondo, com.lowagie.text.Font fuente) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(new com.lowagie.text.Chunk(texto, fuente)));
        cell.setBackgroundColor(fondo);
        cell.setPadding(8);
        return cell;
    }

    // Reemplaza o valida que tu addPdfRow actual esté mapeado así:
    private void addPdfRow(com.lowagie.text.pdf.PdfPTable table, String metric, String value, com.lowagie.text.Font f1, com.lowagie.text.Font f2) {
        com.lowagie.text.pdf.PdfPCell cell1 = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(new com.lowagie.text.Chunk(metric, f1)));
        com.lowagie.text.pdf.PdfPCell cell2 = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(new com.lowagie.text.Chunk(value, f2)));
        cell1.setPadding(8);
        cell2.setPadding(8);
        table.addCell(cell1);
        table.addCell(cell2);
    }
}
