package com.Sistema.Backend.Proveedores.Controllers;

import com.Sistema.Backend.Proveedores.Dto.Request.ProveedorInsumoRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.ProveedorInsumoResponseDTO;
import com.Sistema.Backend.Proveedores.Services.ProveedorInsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedor-insumos")
@Tag(name = "Costos e Insumos de Proveedor", description = "Controlador para administrar el catálogo de costos, unidades de empaque y factores de conversión por proveedor")
public class ProveedorInsumoController {

    private final ProveedorInsumoService proveedorInsumoService;

    public ProveedorInsumoController(ProveedorInsumoService proveedorInsumoService) {
        this.proveedorInsumoService = proveedorInsumoService;
    }

    @GetMapping("/proveedor/{proveedorId}")
    @Operation(summary = "Listar catálogo de un proveedor de forma paginada", description = "Retorna de forma paginada todos los insumos y costos asociados a un proveedor en específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catálogo obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "El proveedor especificado no existe")
    })
    public ResponseEntity<Page<ProveedorInsumoResponseDTO>> listarPorProveedor(
            @PathVariable Long proveedorId,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 8) Pageable pageable) {

        Page<ProveedorInsumoResponseDTO> pagina = proveedorInsumoService.listarPorProveedor(proveedorId, activo, pageable);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/insumo/{insumoId}")
    @Operation(summary = "Buscar proveedores por insumo", description = "Retorna un arreglo plano con todos los proveedores activos que surten un insumo de inventario específico, ordenando sus costos")
    @ApiResponse(responseCode = "200", description = "Lista de proveedores para el insumo procesada")
    public ResponseEntity<List<ProveedorInsumoResponseDTO>> obtenerProveedoresPorInsumo(@PathVariable Long insumoId) {
        List<ProveedorInsumoResponseDTO> lista = proveedorInsumoService.obtenerProveedoresPorInsumo(insumoId);
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/asignar-costo")
    @Operation(summary = "Asignar o actualizar costo de insumo", description = "Vincula un costo y factor de conversión a un binomio proveedor-insumo. Si la relación ya existía lógicamente inactiva, la actualiza y reactiva automáticamente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relación de costo establecida o actualizada con éxito"),
            @ApiResponse(responseCode = "404", description = "El proveedor o insumo proporcionado no existe en el sistema")
    })
    public ResponseEntity<ProveedorInsumoResponseDTO> asignarOActualizarCosto(
            @Valid @RequestBody ProveedorInsumoRequestDTO dto) {
        ProveedorInsumoResponseDTO respuesta = proveedorInsumoService.asignarOActualizarCosto(dto);
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desvincular insumo de proveedor (Baja lógica)", description = "Realiza un soft delete sobre el registro del catálogo del proveedor, removiendo el costo del flujo de compras cotidianas")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Relación dada de baja de forma lógica correctamente"),
            @ApiResponse(responseCode = "404", description = "No se localizó la relación de costo ingresada")
    })
    public ResponseEntity<Void> eliminarRelacion(@PathVariable Long id) {
        proveedorInsumoService.eliminarRelacion(id);
        return ResponseEntity.noContent().build();
    }
}
