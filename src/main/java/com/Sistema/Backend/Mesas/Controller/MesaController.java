package com.Sistema.Backend.Mesas.Controller;

import com.Sistema.Backend.Mesas.Dto.Request.CambioEstadoRequestDTO;
import com.Sistema.Backend.Mesas.Dto.Request.MesaRequestDTO;
import com.Sistema.Backend.Mesas.Dto.Response.MesaResponseDTO;
import com.Sistema.Backend.Mesas.Entity.EstadoMesa;
import com.Sistema.Backend.Mesas.Services.MesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
@Tag(name = "Mesas", description = "Controlador operativo y de administración para la distribución del salón y asignación de comandas")
public class MesaController {

    private final MesaService mesaService;

    // Inyección por constructor idéntica a CategoriaController
    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    @Operation(summary = "Listar mesas", description = "Retorna el listado plano de todas las mesas del salón. Permite filtrar opcionalmente por su estado operativo actual.")
    @ApiResponse(responseCode = "200", description = "Consulta procesada con éxito")
    public ResponseEntity<List<MesaResponseDTO>> listarTodas(@RequestParam(required = false) EstadoMesa estado) {
        List<MesaResponseDTO> lista = (estado != null)
                ? mesaService.obtenerPorEstado(estado)
                : mesaService.obtenerTodas();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener mesa por ID", description = "Busca los metadatos operativos de una mesa específica mediante su clave primaria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mesa localizada con éxito"),
            @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado")
    })
    public ResponseEntity<MesaResponseDTO> obtenerPorId(@PathVariable Long id) {
        MesaResponseDTO dto = mesaService.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @Operation(summary = "Registrar nueva mesa", description = "Crea una nueva mesa en el salón en estado inicial LIBRE validando la unicidad del identificador numérico")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mesa registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Validación de campos fallida o identificador duplicado")
    })
    public ResponseEntity<MesaResponseDTO> crear(@Valid @RequestBody MesaRequestDTO dto) {
        MesaResponseDTO nueva = mesaService.crearMesa(dto);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar propiedades físicas de la mesa", description = "Permite modificar el identificador numérico y la capacidad de comensales")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propiedades actualizadas correctamente"),
            @ApiResponse(responseCode = "404", description = "Mesa no encontrada")
    })
    public ResponseEntity<MesaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MesaRequestDTO dto) {
        MesaResponseDTO actualizada = mesaService.actualizarMesa(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    @PostMapping("/{id}/abrir")
    @Operation(summary = "Abrir mesa (Asignar Comanda)", description = "Cambia el estado de una mesa a OCUPADA vinculando obligatoriamente el mesero responsable y el ID de pedido generado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mesa abierta exitosamente, flujo de comanda activo"),
            @ApiResponse(responseCode = "400", description = "La mesa ya se encuentra ocupada o faltan parámetros mandatorios"),
            @ApiResponse(responseCode = "404", description = "Mesa no encontrada")
    })
    public ResponseEntity<MesaResponseDTO> abrirMesa(
            @PathVariable Long id,
            @RequestParam Long pedidoId,
            @RequestParam Long meseroId) {
        MesaResponseDTO abierta = mesaService.abrirMesa(id, pedidoId, meseroId);
        return ResponseEntity.ok(abierta);
    }

    @PostMapping("/{id}/reservar")
    @Operation(summary = "Reservar una mesa", description = "Bloquea una mesa que esté en estado LIBRE e introduce las anotaciones del cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mesa reservada correctamente"),
            @ApiResponse(responseCode = "400", description = "La mesa seleccionada no está disponible para reserva o las notas están vacías")
    })
    public ResponseEntity<MesaResponseDTO> reservarMesa(
            @PathVariable Long id,
            @RequestBody String notasReserva) {
        MesaResponseDTO reservada = mesaService.reservarMesa(id, notasReserva);
        return ResponseEntity.ok(reservada);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambio de estado rápido", description = "Permite transicionar rápidamente el flujo operativo entre estados (ej. pasar de SUCIA a LIBRE al terminar la limpieza)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado operativo modificado con éxito"),
            @ApiResponse(responseCode = "400", description = "Intento de forzar estado OCUPADA desde este endpoint o payload inválido")
    })
    public ResponseEntity<MesaResponseDTO> cambiarEstadoRapido(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoRequestDTO dto) {
        MesaResponseDTO actualizada = mesaService.cambiarEstadoRapido(id, dto);
        return ResponseEntity.ok(actualizada);
    }
}
