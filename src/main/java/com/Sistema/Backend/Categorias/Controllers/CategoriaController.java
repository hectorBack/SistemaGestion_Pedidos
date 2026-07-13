package com.Sistema.Backend.Categorias.Controllers;

import com.Sistema.Backend.Categorias.Dto.Request.CategoriaRequestDTO;
import com.Sistema.Backend.Categorias.Dto.Response.CategoriaResponseDTO;
import com.Sistema.Backend.Categorias.Services.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Controlador de administración para clasificar productos de los pedidos")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'COCINERO', 'CLIENTE')")
    @Operation(summary = "Listar categorías de forma paginada", description = "Consulta paginada que permite buscar coincidencias de texto y estado de actividad ignorando el filtro de soft delete")
    @ApiResponse(responseCode = "200", description = "Consulta paginada procesada")
    public ResponseEntity<Page<CategoriaResponseDTO>> listarPaginado(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 8) Pageable pageable) {

        Page<CategoriaResponseDTO> pagina = categoriaService.listarPaginado(nombre, activo, pageable);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/todas")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'COCINERO', 'CLIENTE')")
    @Operation(summary = "Listar todas las categorías", description = "Retorna un arreglo plano con las categorías marcadas como activas")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodas() {
        List<CategoriaResponseDTO> lista = categoriaService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'COCINERO', 'CLIENTE')")
    @Operation(summary = "Obtener categoría por ID", description = "Busca un registro específico por su clave primaria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "No se localizó el recurso solicitado")
    })
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(@PathVariable Long id) {
        CategoriaResponseDTO dto = categoriaService.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Registrar nueva categoría", description = "Crea un registro activo o levanta una fila inactiva si coincide el nombre exacto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Validación fallida o nombre ya duplicado")
    })

    public ResponseEntity<CategoriaResponseDTO> crear(@RequestBody CategoriaRequestDTO dto) {
        CategoriaResponseDTO nueva = categoriaService.crear(dto);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Actualizar categoría existente", description = "Modifica los atributos del DTO validando unicidad de nombres")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody CategoriaRequestDTO dto) {
        CategoriaResponseDTO actualizada = categoriaService.actualizar(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Baja lógica de categoría", description = "Cambia el estado de una categoría a inactivo (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Baja lógica ejecutada con éxito"),
            @ApiResponse(responseCode = "404", description = "El ID ingresado no coincide con una categoría activa")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/orden")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
            summary = "Actualizar prioridad de categorías",
            description = "Recibe un arreglo plano con los IDs ordenados según el Drag and Drop de la interfaz y reasigna su prioridad masivamente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden del menú actualizado con éxito"),
            @ApiResponse(responseCode = "400", description = "La lista de IDs proporcionada no es válida")
    })
    public ResponseEntity<Void> actualizarOrden(@RequestBody List<Long> idsOrdenados) {
        categoriaService.actualizarOrden(idsOrdenados);
        return ResponseEntity.ok().build();
    }
}
