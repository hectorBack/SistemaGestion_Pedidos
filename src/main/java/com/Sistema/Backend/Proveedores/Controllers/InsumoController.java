package com.Sistema.Backend.Proveedores.Controllers;

import com.Sistema.Backend.Proveedores.Dto.Request.InsumoRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.InsumoResponseDTO;
import com.Sistema.Backend.Proveedores.Services.InsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insumos")
@Tag(name = "Insumos", description = "Controlador de administración para la materia prima global del restaurante")
public class InsumoController {

    private final InsumoService insumoService;

    public InsumoController(InsumoService insumoService) {
        this.insumoService = insumoService;
    }

    @GetMapping
    @Operation(summary = "Listar insumos de forma paginada", description = "Consulta paginada que permite buscar coincidencias de texto y estado de actividad ignorando el filtro de soft delete")
    @ApiResponse(responseCode = "200", description = "Consulta paginada procesada")
    public ResponseEntity<Page<InsumoResponseDTO>> listarPaginado(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 8) Pageable pageable) {

        Page<InsumoResponseDTO> pagina = insumoService.obtenerInsumosPaginados(nombre, activo, pageable);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/todos")
    @Operation(summary = "Listar todos los insumos activos", description = "Retorna un arreglo plano con los insumos marcados como activos (Ideal para selects)")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    public ResponseEntity<List<InsumoResponseDTO>> listarTodos() {
        List<InsumoResponseDTO> lista = insumoService.obtenerTodosLosActivos();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener insumo por ID", description = "Busca un registro específico por su clave primaria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumo encontrado"),
            @ApiResponse(responseCode = "404", description = "No se localizó el recurso solicitado")
    })
    public ResponseEntity<InsumoResponseDTO> obtenerPorId(@PathVariable Long id) {
        InsumoResponseDTO dto = insumoService.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo insumo", description = "Crea un registro activo en el almacén global")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Validación fallida o datos inválidos")
    })
    public ResponseEntity<InsumoResponseDTO> crear(@Valid @RequestBody InsumoRequestDTO dto) {
        InsumoResponseDTO nueva = insumoService.crearInsumo(dto);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar insumo existente", description = "Modifica los atributos del DTO validando unicidad y consistencia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Insumo no encontrado")
    })
    public ResponseEntity<InsumoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody InsumoRequestDTO dto) {
        InsumoResponseDTO actualizada = insumoService.actualizarInsumo(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja lógica de insumo", description = "Cambia el estado de un insumo a inactivo (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Baja lógica ejecutada con éxito"),
            @ApiResponse(responseCode = "404", description = "El ID ingresado no coincide con un insumo activo")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        insumoService.eliminarInsumo(id);
        return ResponseEntity.noContent().build();
    }
}
