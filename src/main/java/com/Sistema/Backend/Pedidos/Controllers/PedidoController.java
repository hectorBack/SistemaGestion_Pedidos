package com.Sistema.Backend.Pedidos.Controllers;

import com.Sistema.Backend.Pedidos.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Pedidos.Entity.EstadoPedido;
import com.Sistema.Backend.Pedidos.Services.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // 1. Crear pedido (El que usará el cliente desde el link)
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody PedidoRequestDTO request) {
        return new ResponseEntity<>(pedidoService.crearPedido(request), HttpStatus.CREATED);
    }

    // 2. Obtener pedidos activos (Para el monitor de cocina)
    @GetMapping("/activos")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(pedidoService.obtenerPedidosActivos());
    }

    // 3. Actualizar estado (Para pasar de PENDIENTE a EN_COCINA, etc.)
    @PatchMapping("/{id}/estado/{nuevoEstado}")
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @PathVariable EstadoPedido nuevoEstado) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, nuevoEstado));
    }

    // 4. Buscar por WhatsApp (Búsqueda rápida en local)
    @GetMapping("/buscar/whatsapp/{digitos}")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorWhatsapp(@PathVariable String digitos) {
        return ResponseEntity.ok(pedidoService.buscarPorWhatsapp(digitos));
    }

    // 5. Historial completo
    @GetMapping("/historial")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerHistorial() {
        return ResponseEntity.ok(pedidoService.obtenerHistorialTodos());
    }

    // 6. Cancelar pedido
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

    // 7. Ventas del día
    @GetMapping("/ventas/hoy")
    public ResponseEntity<BigDecimal> obtenerVentasDia() {
        return ResponseEntity.ok(pedidoService.calcularTotalVentasDelDia());
    }

    // 8. filtrador
    @GetMapping("/filtrar")
    public ResponseEntity<Page<PedidoResponseDTO>> filtrar(
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Pageable pageable) {

        return ResponseEntity.ok(pedidoService.obtenerPedidosFiltrados(estado, inicio, fin, pageable));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<PedidoResponseDTO> obtenerPorCodigo(@PathVariable String codigo) {
        PedidoResponseDTO response = pedidoService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(response);
    }

}
