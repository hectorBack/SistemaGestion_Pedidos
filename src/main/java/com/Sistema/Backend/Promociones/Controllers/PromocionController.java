package com.Sistema.Backend.Promociones.Controllers;

import com.Sistema.Backend.Promociones.Dto.PromocionStatsDTO;
import com.Sistema.Backend.Promociones.Dto.Request.PromocionRequestDTO;
import com.Sistema.Backend.Promociones.Dto.Response.PromocionResponseDTO;
import com.Sistema.Backend.Promociones.Services.PromocionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")
@Tag(name = "Promociones", description = "Controlador para la gestión, vigencia y asignación de descuentos comerciales")
public class PromocionController {

    private final PromocionService promocionService;

    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    //Listar Paginado
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO')")
    @Operation(summary = "Listar promociones con filtros dinámicos (Paginado)", description = "Consulta avanzada para el panel de control con soporte de filtros por nombre y estado de actividad")
    @ApiResponse(responseCode = "200", description = "Página de promociones procesada con éxito")
    public ResponseEntity<Page<PromocionResponseDTO>> listarPromociones(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activa,
            @PageableDefault(size = 8) Pageable pageable) { // Unificado para usar PageableDefault estándar

        Page<PromocionResponseDTO> promociones = promocionService.listarPaginado(nombre, activa, pageable);
        return ResponseEntity.ok(promociones);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Registrar nueva promoción", description = "Crea una promoción parametrizada. Si no se envía un productoId, se asume que el descuento es global")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Promoción registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada del DTO inconsistentes")
    })
    public PromocionResponseDTO crear(@Valid @RequestBody PromocionRequestDTO dto) {
        return promocionService.crearPromocion(dto);
    }

    @GetMapping("/vigentes")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'CLIENTE')")
    @Operation(summary = "Obtener promociones vigentes", description = "Retorna un arreglo plano con las promociones que se intersectan de forma válida con la fecha y hora actual")
    @ApiResponse(responseCode = "200", description = "Promociones vigentes recuperadas")
    public List<PromocionResponseDTO> getPromocionesVigentes() {
        return promocionService.listarPromocionesVigentes();
    }

    // Actualizar promocion completo
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Actualizar promoción completa", description = "Reemplaza los atributos de una promoción y valida la existencia del producto asignado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promoción actualizada correctamente"),
            @ApiResponse(responseCode = "404", description = "ID de la promoción o del producto no localizados")
    })
        public ResponseEntity<PromocionResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PromocionRequestDTO request){
        return ResponseEntity.ok(promocionService.actualizarPromocion(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Desactivar promoción", description = "Cambia el flag de actividad a falso de manera inmediata para pausar el descuento comercial")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Promoción pausada exitosamente"),
            @ApiResponse(responseCode = "404", description = "El ID de la promoción no existe en la base de datos")
    })
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        promocionService.desactivarPromocion(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Activar promoción", description = "Cambia el flag de actividad a verdadero de manera inmediata para reanudar el descuento comercial")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Promoción activada exitosamente"),
            @ApiResponse(responseCode = "404", description = "El ID de la promoción no existe en la base de datos")
    })
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        promocionService.activarPromocion(id); // O el método correspondiente en tu servicio que ponga el flag en true
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Obtener estadísticas globales", description = "Retorna el conteo consolidado de todo el universo de promociones")
    public ResponseEntity<PromocionStatsDTO> getStats() {
        return ResponseEntity.ok(promocionService.obtenerEstadisticasGlobales());
    }
}
