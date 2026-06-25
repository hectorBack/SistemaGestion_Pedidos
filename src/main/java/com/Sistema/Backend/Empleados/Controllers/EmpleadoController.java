package com.Sistema.Backend.Empleados.Controllers;

import com.Sistema.Backend.Empleados.Dto.Request.EmpleadoRequestDTO;
import com.Sistema.Backend.Empleados.Dto.Response.EmpleadoResponseDTO;
import com.Sistema.Backend.Empleados.Entity.PuestoEmpleado;
import com.Sistema.Backend.Empleados.Services.EmpleadoService;
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
@RequestMapping("/api/empleados")
@Tag(name = "Empleados", description = "Controlador de administración para la gestión y asignación del personal del restaurante")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping
    @Operation(summary = "Listar empleados de forma paginada", description = "Consulta paginada con filtros opcionales por nombre, puesto y estado de actividad")
    @ApiResponse(responseCode = "200", description = "Consulta paginada procesada con éxito")
    public ResponseEntity<Page<EmpleadoResponseDTO>> listarPaginado(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) PuestoEmpleado puesto,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 8) Pageable pageable) {

        Page<EmpleadoResponseDTO> pagina = empleadoService.listarPaginado(nombre, puesto, activo, pageable);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar empleados activos por puesto", description = "Retorna un arreglo plano con los empleados activos filtrados por su puesto (ideal para selectores en comandas)")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    public ResponseEntity<List<EmpleadoResponseDTO>> listarActivosPorPuesto(
            @RequestParam PuestoEmpleado puesto) {

        List<EmpleadoResponseDTO> lista = empleadoService.listarActivosPorPuesto(puesto);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empleado por ID", description = "Busca un trabajador específico mediante su clave primaria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empleado localizado"),
            @ApiResponse(responseCode = "404", description = "No se encontró el empleado solicitado")
    })
    public ResponseEntity<EmpleadoResponseDTO> obtenerPorId(@PathVariable Long id) {
        EmpleadoResponseDTO dto = empleadoService.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo empleado", description = "Inserta un nuevo trabajador en el sistema en estado activo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empleado creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Cuerpo de la petición inválido o validación fallida")
    })
    public ResponseEntity<EmpleadoResponseDTO> crear(@Valid @RequestBody EmpleadoRequestDTO dto) {
        EmpleadoResponseDTO nueva = empleadoService.crear(dto);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empleado existente", description = "Modifica los atributos del empleado según el ID proporcionado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cambios guardados correctamente"),
            @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    public ResponseEntity<EmpleadoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoRequestDTO dto) {
        EmpleadoResponseDTO actualizada = empleadoService.actualizar(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    @PatchMapping("/{id}/disponibilidad")
    @Operation(summary = "Cambiar disponibilidad/actividad del empleado", description = "Permite activar o desactivar de forma rápida a un empleado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad modificada con éxito"),
            @ApiResponse(responseCode = "404", description = "Empleado no localizado")
    })
    public ResponseEntity<Void> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        empleadoService.cambiarDisponibilidad(id, activo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja lógica de empleado", description = "Realiza un soft delete cambiando el flag de actividad a falso")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Baja lógica ejecutada de manera conforme"),
            @ApiResponse(responseCode = "404", description = "El ID ingresado no corresponde a un empleado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empleadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
