package com.Sistema.Backend.Proveedores.Controllers;

import com.Sistema.Backend.Proveedores.Dto.Request.ProveedorRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.ProveedorResponseDTO;
import com.Sistema.Backend.Proveedores.Services.ProveedorService;
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
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Controlador de administración para el directorio de proveedores de insumos")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    @Operation(summary = "Listar proveedores de forma paginada", description = "Consulta paginada que permite buscar coincidencias de texto por nombre y filtrar por estado de actividad")
    @ApiResponse(responseCode = "200", description = "Consulta paginada procesada con éxito")
    public ResponseEntity<Page<ProveedorResponseDTO>> listarPaginado(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 8) Pageable pageable) {

        Page<ProveedorResponseDTO> pagina = proveedorService.listarPaginado(nombre, activo, pageable);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar todos los proveedores activos", description = "Retorna un arreglo plano con los proveedores marcados como activos, ideal para selectores en la interfaz")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    public ResponseEntity<List<ProveedorResponseDTO>> listarTodosActivos() {
        List<ProveedorResponseDTO> lista = proveedorService.listarTodosActivos();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor por ID", description = "Busca un registro específico en el directorio mediante su clave primaria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado"),
            @ApiResponse(responseCode = "404", description = "No se localizó el proveedor con el ID solicitado")
    })
    public ResponseEntity<ProveedorResponseDTO> obtenerPorId(@PathVariable Long id) {
        ProveedorResponseDTO dto = proveedorService.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo proveedor", description = "Crea una nueva entidad en el directorio validando que el nombre no se encuentre duplicado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Validación de datos fallida o nombre ya registrado")
    })
    public ResponseEntity<ProveedorResponseDTO> crear(@Valid @RequestBody ProveedorRequestDTO dto) {
        ProveedorResponseDTO nueva = proveedorService.crear(dto);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proveedor existente", description = "Modifica los atributos del proveedor validando la unicidad del nombre contra otros registros")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<ProveedorResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequestDTO dto) {
        ProveedorResponseDTO actualizada = proveedorService.actualizar(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja lógica de proveedor", description = "Cambia el estado del proveedor a inactivo (Soft Delete) sin comprometer la integridad histórica")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Baja lógica ejecutada con éxito"),
            @ApiResponse(responseCode = "404", description = "El ID ingresado no coincide con un proveedor activo")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
