package com.Sistema.Backend.Pedidos.Controllers;

import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Pedidos.Dto.Request.AgregarItemsRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Pedidos.Entity.EstadoPedido;
import com.Sistema.Backend.Pedidos.Services.PedidoService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Controlador de administración para la gestión, estados e historial de las comandas y pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo pedido / Apertura de mesa", description = "Crea una comanda inicial o un pedido desde el link del cliente asignando estado PENDIENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido registrado con éxito"),
            @ApiResponse(responseCode = "400", description = "Validación fallida en los datos enviados")
    })
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody PedidoRequestDTO request) {
        return new ResponseEntity<>(pedidoService.crearPedido(request), HttpStatus.CREATED);
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar pedidos activos", description = "Retorna una lista plana de pedidos que se encuentran en preparación, cocina o pendientes para el monitor")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(pedidoService.obtenerPedidosActivos());
    }

    @PatchMapping("/{id}/estado/{nuevoEstado}")
    @Operation(summary = "Actualizar estado del pedido", description = "Modifica únicamente el estado operativo de la orden (ej: pasar de PENDIENTE a EN_COCINA o ENTREGADO)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado modificado correctamente"),
            @ApiResponse(responseCode = "404", description = "No se localizó el pedido con el ID ingresado")
    })
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @PathVariable EstadoPedido nuevoEstado) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, nuevoEstado));
    }

    @GetMapping("/buscar/whatsapp/{digitos}")
    @Operation(summary = "Buscar pedidos por celular/WhatsApp", description = "Realiza una consulta rápida filtrando por las últimas cifras del número telefónico del cliente")
    @ApiResponse(responseCode = "200", description = "Resultados obtenidos")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorWhatsapp(@PathVariable String digitos) {
        return ResponseEntity.ok(pedidoService.buscarPorWhatsapp(digitos));
    }

    @GetMapping("/historial")
    @Operation(summary = "Obtener historial completo", description = "Consulta sin filtros que devuelve el listado histórico de todas las comandas")
    @ApiResponse(responseCode = "200", description = "Historial procesado")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerHistorial() {
        return ResponseEntity.ok(pedidoService.obtenerHistorialTodos());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar pedido", description = "Elimina de forma lógica o física un pedido del sistema liberando recursos asociados")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido cancelado con éxito"),
            @ApiResponse(responseCode = "404", description = "El ID ingresado no coincide con ningún registro")
    })
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ventas/hoy")
    @Operation(summary = "Calcular ventas del día", description = "Suma el total de ingresos monetarios recaudados a partir de los pedidos liquidados hoy")
    @ApiResponse(responseCode = "200", description = "Cálculo financiero exitoso")
    public ResponseEntity<BigDecimal> obtenerVentasDia() {
        return ResponseEntity.ok(pedidoService.calcularTotalVentasDelDia());
    }

    @GetMapping("/filtrar")
    @Operation(summary = "Filtrar pedidos de forma avanzada y paginada", description = "Permite buscar pedidos segmentando opcionalmente por estado y obligatoriamente por un rango de fechas")
    @ApiResponse(responseCode = "200", description = "Consulta paginada devuelta")
    public ResponseEntity<Page<PedidoResponseDTO>> filtrar(
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Pageable pageable) {
        return ResponseEntity.ok(pedidoService.obtenerPedidosFiltrados(estado, inicio, fin, pageable));
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar pedido por código alfanumérico", description = "Busca una orden específica mediante su código único de seguimiento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido localizado"),
            @ApiResponse(responseCode = "404", description = "Código no registrado en el sistema")
    })
    public ResponseEntity<PedidoResponseDTO> obtenerPorCodigo(@PathVariable String codigo) {
        PedidoResponseDTO response = pedidoService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/agregar-items")
    @Operation(summary = "Agregar ítems/segunda ronda a un pedido activo", description = "Añade nuevos productos a una comanda ya abierta, recalculando automáticamente el total acumulado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ítems añadidos y cuenta actualizada"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe o ya fue cerrado previamente")
    })
    public ResponseEntity<PedidoResponseDTO> agregarItems(
            @PathVariable Long id,
            @Valid @RequestBody AgregarItemsRequestDTO request) {
        PedidoResponseDTO respuesta = pedidoService.agregarItemsAPedido(id, request);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/activo/mesa/{mesaId}")
    @Operation(summary = "Obtener pedido activo de una mesa", description = "Busca si existe una cuenta abierta vinculada al ID de la mesa proporcionada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido activo encontrado"),
            @ApiResponse(responseCode = "204", description = "La mesa se encuentra libre o sin pedidos activos")
    })
    public ResponseEntity<PedidoResponseDTO> obtenerPedidoActivoPorMesa(@PathVariable Long mesaId) {
        try {
            PedidoResponseDTO respuesta = pedidoService.obtenerPedidoActivoPorMesa(mesaId);
            return ResponseEntity.ok(respuesta);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
    }
}
