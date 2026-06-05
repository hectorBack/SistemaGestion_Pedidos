package com.Sistema.Backend.Pagos.Controller;

import com.Sistema.Backend.Pagos.Dto.Request.PagoRequestDTO;
import com.Sistema.Backend.Pagos.Dto.Response.PagoResponseDTO;
import com.Sistema.Backend.Pagos.Entity.MetodoPago;
import com.Sistema.Backend.Pagos.Services.PagoService;
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
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    // 🌟 1. Registrar un nuevo pago (Caja / Punto de Venta)
    @PostMapping
    public ResponseEntity<PagoResponseDTO> registrarPago(@Valid @RequestBody PagoRequestDTO pagoRequestDTO) {
        PagoResponseDTO response = pagoService.registrarPago(pagoRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 🌟 2. Obtener un pago por su código público de transacción (e.g., PAG-8A3F1)
    @GetMapping("/codigo/{codigoTransaccion}")
    public ResponseEntity<PagoResponseDTO> obtenerPorCodigo(@PathVariable String codigoTransaccion) {
        PagoResponseDTO response = pagoService.obtenerPorCodigo(codigoTransaccion);
        return ResponseEntity.ok(response);
    }

    // 🌟 3. Obtener el pago asociado a un pedido específico usando el ID del pedido
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<PagoResponseDTO> obtenerPorPedidoId(@PathVariable Long pedidoId) {
        PagoResponseDTO response = pagoService.obtenerPorPedidoId(pedidoId);
        return ResponseEntity.ok(response);
    }

    // 🌟 4. Listar todos los pagos (Útil para reportería o paneles de administración)
    @GetMapping("/filtrar")
    public ResponseEntity<Page<PagoResponseDTO>> filtrarPagos(
            @RequestParam(required = false) MetodoPago metodo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Pageable pageable) {

        return ResponseEntity.ok(pagoService.obtenerPagosFiltrados(metodo, inicio, fin, pageable));
    }
}
