package com.Sistema.Backend.Reportes.Controllers;

import com.Sistema.Backend.Reportes.Dto.ReporteVentasDTO;
import com.Sistema.Backend.Reportes.Services.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Controlador para la consolidación analítica del negocio y exportación de archivos ejecutivos")
@PreAuthorize("hasAuthority('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/ventas")
    @Operation(summary = "Obtener resumen analítico de ventas", description = "Consolida las métricas financieras (Ingresos, Ticket Promedio) y operativas dentro de un rango de fecha y hora")
    @ApiResponse(responseCode = "200", description = "Payload estructurado con la analítica consolidada")
    public ResponseEntity<ReporteVentasDTO> obtenerReporte(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        return ResponseEntity.ok(reporteService.generarResumenVentas(inicio, fin));
    }

    @GetMapping("/exportar/excel")
    @Operation(summary = "Exportar reporte a MS Excel", description = "Genera y descarga un libro en formato .xlsx con las gráficas de KPI estructuradas mediante Apache POI")
    @ApiResponse(responseCode = "200", description = "Libro de Excel descargado de forma binaria")
    public ResponseEntity<InputStreamResource> descargarExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        ByteArrayInputStream stream = reporteService.exportarReporteExcel(inicio, fin);

        HttpHeaders headers = new HttpHeaders();
        // Le indicamos al navegador que es un archivo adjunto descargable
        headers.add("Content-Disposition", "attachment; filename=Reporte_Ventas_" + inicio.toLocalDate() + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/exportar/pdf")
    @Operation(summary = "Exportar reporte a PDF", description = "Compila y descarga un documento estructurado de iText en formato PDF A4 listo para impresión ejecutiva")
    @ApiResponse(responseCode = "200", description = "Documento PDF descargado de forma binaria")
    public ResponseEntity<InputStreamResource> descargarPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        ByteArrayInputStream stream = reporteService.exportarReportePDF(inicio, fin);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Reporte_Ventas_" + inicio.toLocalDate() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(stream));
    }
}
