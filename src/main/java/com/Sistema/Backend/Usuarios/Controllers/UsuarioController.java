package com.Sistema.Backend.Usuarios.Controllers;

import com.Sistema.Backend.Usuarios.Dto.Request.UsuarioRequestDTO;
import com.Sistema.Backend.Usuarios.Dto.Response.UsuarioResponseDTO;
import com.Sistema.Backend.Usuarios.Services.UsuarioService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Controlador de administración para la gestión de empleados y credenciales del sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Listar usuarios de forma paginada", description = "Consulta paginada orientada a la tabla de administración para visualizar el personal activo o inactivo")
    @ApiResponse(responseCode = "200", description = "Consulta paginada procesada")
    public ResponseEntity<Page<UsuarioResponseDTO>> listarPaginado(
            @PageableDefault(size = 8) Pageable pageable) {

        Page<UsuarioResponseDTO> pagina = usuarioService.listarUsuariosPaginados(pageable);
        return ResponseEntity.ok(pagina);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea un nuevo empleado con sus respectivas credenciales y roles asignados")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos o el username ya existe")
    })
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO nuevoUsuario = usuarioService.registrarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Obtener usuario por ID", description = "Busca un usuario específico del sistema por su clave primaria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado con éxito"),
            @ApiResponse(responseCode = "404", description = "No se localizó el usuario solicitado")
    })
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Cambiar estado de actividad de forma parcial", description = "Permite activar o desactivar una cuenta de empleado de forma lógica (baja del personal) usando PATCH")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado modificado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo) {

        usuarioService.cambiarEstadoUsuario(id, activo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Eliminación física de usuario", description = "Remueve por completo el registro de la base de datos de manera irreversible")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado físicamente con éxito"),
            @ApiResponse(responseCode = "404", description = "El ID ingresado no corresponde a ningún usuario existente")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
