package com.Sistema.Backend.Clientes.Controllers;

import com.Sistema.Backend.Clientes.Dto.Request.ClienteRequestDTO;
import com.Sistema.Backend.Clientes.Dto.Response.ClienteResponseDTO;
import com.Sistema.Backend.Clientes.Services.ClienteService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Controlador de administración exclusivo para la gestión de perfiles de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Listar clientes de forma paginada", description = "Consulta paginada que permite buscar coincidencias de texto por nombre, email o username, además de filtrar por estado lógico")
    @ApiResponse(responseCode = "200", description = "Consulta paginada procesada")
    public ResponseEntity<Page<ClienteResponseDTO>> listarPaginado(
            @RequestParam(name = "nombre", required = false) String filtro,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 8) Pageable pageable) {

        // El resto se queda exactamente igual
        Page<ClienteResponseDTO> pagina = clienteService.listarClientesPaginados(filtro, activo, pageable);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Obtener cliente por ID", description = "Busca un perfil específico de cliente por su clave primaria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "No se localizó el recurso solicitado")
    })
    public ResponseEntity<ClienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        ClienteResponseDTO dto = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    // 💡 Nota: Quitamos @PreAuthorize si es un endpoint público para Registrar.vue.
    // Si este Post lo usas solo desde el panel del Admin, agrégale: @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Registrar nuevo cliente público", description = "Crea una cuenta e interactúa asignando de manera obligatoria y automática el rol CLIENTE en el backend")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Validación fallida, email o nombre de usuario duplicado")
    })
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO nueva = clienteService.registrarClientePublico(dto);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Actualizar cliente existente", description = "Modifica los atributos del perfil y credenciales validando la unicidad del email y username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO actualizada = clienteService.actualizarCliente(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Baja lógica de cliente", description = "Desactiva lógicamente el perfil del cliente y su cuenta de usuario asociada (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Baja lógica ejecutada con éxito"),
            @ApiResponse(responseCode = "404", description = "El ID ingresado no coincide con un cliente existente")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.cambiarEstadoActivo(id, false);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Reactivar cliente suspendido", description = "Devuelve el estado activo en cascada al perfil de cliente y su usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente reactivado con éxito"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        clienteService.cambiarEstadoActivo(id, true);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Cambiar estado de actividad dinámico", description = "Activa o desactiva lógicamente el perfil de un cliente basado en el valor booleano enviado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado con éxito"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {

        Boolean nuevoEstado = body.get("activo");
        if (nuevoEstado == null) {
            return ResponseEntity.badRequest().build();
        }

        clienteService.cambiarEstadoActivo(id, nuevoEstado);
        return ResponseEntity.ok().build();
    }
}
