package com.Sistema.Backend.Controllers;

import com.Sistema.Backend.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Entity.EstadoPedido;
import com.Sistema.Backend.Services.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPedido nuevoEstado) {
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

}
