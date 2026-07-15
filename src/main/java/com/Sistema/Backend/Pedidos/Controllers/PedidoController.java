package com.Sistema.Backend.Pedidos.Controllers;

import com.Sistema.Backend.Clientes.Entity.Cliente;
import com.Sistema.Backend.Clientes.Repository.ClienteRepository;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Pedidos.Dto.Request.AgregarItemsRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Pedidos.Entity.EstadoPedido;
import com.Sistema.Backend.Pedidos.Services.PedidoService;
import com.Sistema.Backend.Usuarios.Entity.Usuario;
import com.Sistema.Backend.Usuarios.Repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Controlador de administración para la gestión, estados e historial de las comandas y pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoController(PedidoService pedidoService, ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.pedidoService = pedidoService;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'CLIENTE')")
    @Operation(summary = "Registrar nuevo pedido / Apertura de mesa", description = "Crea una comanda inicial o un pedido desde el link del cliente asignando estado PENDIENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido registrado con éxito"),
            @ApiResponse(responseCode = "400", description = "Validación fallida en los datos enviados")
    })
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody PedidoRequestDTO request) {
        return new ResponseEntity<>(pedidoService.crearPedido(request), HttpStatus.CREATED);
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'COCINERO')")
    @Operation(summary = "Listar pedidos activos", description = "Retorna una lista plana de pedidos que se encuentran en preparación, cocina o pendientes para el monitor")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(pedidoService.obtenerPedidosActivos());
    }

    @PatchMapping("/{id}/estado/{nuevoEstado}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'COCINERO')")
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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'CLIENTE')")
    @Operation(summary = "Buscar pedidos por celular/WhatsApp", description = "Realiza una consulta rápida filtrando por las últimas cifras del número telefónico del cliente")
    @ApiResponse(responseCode = "200", description = "Resultados obtenidos")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorWhatsapp(@PathVariable String digitos) {
        return ResponseEntity.ok(pedidoService.buscarPorWhatsapp(digitos));
    }

    @GetMapping("/historial")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Obtener historial completo", description = "Consulta sin filtros que devuelve el listado histórico de todas las comandas")
    @ApiResponse(responseCode = "200", description = "Historial procesado")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerHistorial() {
        return ResponseEntity.ok(pedidoService.obtenerHistorialTodos());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO')")
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
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Calcular ventas del día", description = "Suma el total de ingresos monetarios recaudados a partir de los pedidos liquidados hoy")
    @ApiResponse(responseCode = "200", description = "Cálculo financiero exitoso")
    public ResponseEntity<BigDecimal> obtenerVentasDia() {
        return ResponseEntity.ok(pedidoService.calcularTotalVentasDelDia());
    }

    @GetMapping("/filtrar")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO')")
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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'CLIENTE')")
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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'CLIENTE')")
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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'CLIENTE')")
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

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasAuthority('CLIENTE')")
    public ResponseEntity<Page<PedidoResponseDTO>> obtenerMisPedidos(Authentication authentication,
                                                                     @RequestParam(required = false, defaultValue = "TODOS") String estado,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "5") int size) {
        String nombreCliente = authentication.getName();

        // Ordenamos siempre de manera descendente por fecha de creación
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());

        // 2. Con el nombre del cliente, traemos sus pedidos
        Page<PedidoResponseDTO> historial = pedidoService.obtenerHistorialClientePaginado(nombreCliente, estado, pageable);
        return ResponseEntity.ok(historial);
    }
}
