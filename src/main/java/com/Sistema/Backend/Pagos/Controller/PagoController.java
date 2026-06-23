package com.Sistema.Backend.Pagos.Controller;

import com.Sistema.Backend.Pagos.Dto.ReembolsoRequestDTO;
import com.Sistema.Backend.Pagos.Dto.Request.PagoRequestDTO;
import com.Sistema.Backend.Pagos.Dto.Response.PagoResponseDTO;
import com.Sistema.Backend.Pagos.Entity.MetodoPago;
import com.Sistema.Backend.Pagos.Services.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Controlador encargado del registro de transacciones financieras y cierres de caja")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    // Registrar un nuevo pago (Caja / Punto de Venta)
    @PostMapping
    @Operation(summary = "Registrar un nuevo pago", description = "Registra la transacción de una venta en el Punto de Venta. Si el pago es exitoso, dispara la orden a cocina de forma interna")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago procesado y registrado con éxito"),
            @ApiResponse(responseCode = "404", description = "El ID del pedido ingresado no pertenece a ningún registro")
    })
    public ResponseEntity<PagoResponseDTO> registrarPago(@Valid @RequestBody PagoRequestDTO pagoRequestDTO) {
        PagoResponseDTO response = pagoService.registrarPago(pagoRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Obtener un pago por su código público de transacción (e.g., PAG-8A3F1)
    @GetMapping("/codigo/{codigoTransaccion}")
    @Operation(summary = "Obtener pago por su código de transacción", description = "Busca los metadatos de un cobro utilizando el hash o código alfanumérico público")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacción localizada"),
            @ApiResponse(responseCode = "404", description = "No se encontró ningún registro bajo el código provisto")
    })
    public ResponseEntity<PagoResponseDTO> obtenerPorCodigo(@PathVariable String codigoTransaccion) {
        PagoResponseDTO response = pagoService.obtenerPorCodigo(codigoTransaccion);
        return ResponseEntity.ok(response);
    }

    // 3. Obtener el pago asociado a un pedido específico usando el ID del pedido
    @GetMapping("/pedido/{pedidoId}")
    @Operation(summary = "Obtener pago por ID del Pedido", description = "Permite inspeccionar el método de pago o el estado de cobro de un ticket específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago asociado encontrado"),
            @ApiResponse(responseCode = "404", description = "El pedido no cuenta con transacciones de pago asentadas")
    })
    public ResponseEntity<PagoResponseDTO> obtenerPorPedidoId(@PathVariable Long pedidoId) {
        PagoResponseDTO response = pagoService.obtenerPorPedidoId(pedidoId);
        return ResponseEntity.ok(response);
    }

    // 4. Listar todos los pagos (Útil para reportería o paneles de administración)
    @GetMapping("/filtrar")
    @Operation(summary = "Filtrar y auditar pagos (Reportería)", description = "Consulta paginada obligatoria por rangos de fechas (Formato ISO YYYY-MM-DD) y método de pago opcional para auditorías financieras")
    @ApiResponse(responseCode = "200", description = "Listado de auditoría generado correctamente")
    public ResponseEntity<Page<PagoResponseDTO>> filtrarPagos(
            @RequestParam(required = false) MetodoPago metodo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Pageable pageable) {

        return ResponseEntity.ok(pagoService.obtenerPagosFiltrados(metodo, inicio, fin, pageable));
    }

    @PatchMapping("/{id}/reembolsar")
    @Operation(summary = "Reembolsar una transacción aprobada", description = "Cambia el estado de un pago a REEMBOLSADO y añade las observaciones correspondientes en la bitácora de caja.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacción reembolsada con éxito"),
            @ApiResponse(responseCode = "404", description = "El ID del pago no pertenece a ningún registro válido"),
            @ApiResponse(responseCode = "400", description = "La transacción no cumple con las condiciones para ser reembolsada (ej. ya estaba rechazada o reembolsada)")
    })
    public ResponseEntity<PagoResponseDTO> reembolsarPago(
            @PathVariable Long id,
            @Valid @RequestBody ReembolsoRequestDTO reembolsoRequest) {

        PagoResponseDTO response = pagoService.reembolsarPago(id, reembolsoRequest);
        return ResponseEntity.ok(response);
    }
}
