package com.Sistema.Backend.Productos.Controllers;

import com.Sistema.Backend.Categorias.Dto.MenuCategoriaDTO;
import com.Sistema.Backend.Productos.Dto.Request.ProductoRequestDTO;
import com.Sistema.Backend.Productos.Dto.Response.ProductoResponseDTO;
import com.Sistema.Backend.Productos.Services.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Controlador para la gestión completa y venta del catálogo de productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // 1. Crear producto (Panel Admin)
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Crear nuevo producto", description = "Registra un producto vinculándolo a una categoría existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado de manera exitosa"),
            @ApiResponse(responseCode = "400", description = "Cuerpo del JSON inválido o error en tipos de datos")
    })
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO request) {
        return new ResponseEntity<>(productoService.crear(request), HttpStatus.CREATED);
    }

    // 2. Listar Paginado (Panel Admin)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'COCINERO')")
    @Operation(summary = "Listar productos con filtros dinámicos (Paginado)", description = "Consulta avanzada para el panel de administración con soporte de filtros por nombre, categoría y disponibilidad")
    @ApiResponse(responseCode = "200", description = "Página de productos procesada")
    public ResponseEntity<Page<ProductoResponseDTO>> listarProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean disponible,
            @org.springframework.data.web.PageableDefault(size = 8) Pageable pageable) {

        Page<ProductoResponseDTO> productos = productoService.listarPaginado(nombre, categoriaId, disponible, pageable);
        return ResponseEntity.ok(productos);
    }

    // 3. Listar solo disponibles (Para el cliente/link)
    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'COCINERO', 'CLIENTE')")
    @Operation(summary = "Listar productos activos para comercialización", description = "Retorna un arreglo plano con todos los productos listos para la venta")
    @ApiResponse(responseCode = "200", description = "Catálogo comercial recuperado")
    public ResponseEntity<List<ProductoResponseDTO>> listarDisponibles() {
        return ResponseEntity.ok(productoService.listarDisponibles());
    }

    // 4. Menú agrupado por categorías (¡Ideal para el Link del Cliente!)
    @GetMapping("/menu")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'COCINERO', 'CLIENTE')")
    @Operation(
            summary = "Obtener menú estructurado y ordenado",
            description = "Retorna el catálogo disponible encapsulado en una lista que respeta la prioridad asignada mediante Drag and Drop"
    )
    @ApiResponse(responseCode = "200", description = "Menú ordenado jerárquicamente generado")
    // CAMBIO AQUÍ: Ahora devuelve un ResponseEntity con una List de MenuCategoriaDTO
    public ResponseEntity<List<MenuCategoriaDTO>> obtenerMenuPorCategorias() {
        return ResponseEntity.ok(productoService.obtenerMenuDigital());
    }

    // 5. Obtener por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'COCINERO', 'CLIENTE')")
    @Operation(summary = "Obtener producto por ID", description = "Busca un producto específico mediante su clave primaria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto localizado"),
            @ApiResponse(responseCode = "404", description = "No se encontró el ID del producto solicitado")
    })
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // 6. Actualizar producto completo
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Actualizar producto existente", description = "Reemplaza todos los datos del producto e inspecciona la clave de la categoría")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto o categoría no encontrados")
    })
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    // 7. Cambiar solo disponibilidad (Switch rápido en Admin)
    @PatchMapping("/{id}/disponibilidad")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'COCINERO')")
    @Operation(summary = "Modificar disponibilidad (Switch rápido)", description = "Permite habilitar o deshabilitar la venta de un producto de manera inmediata sin editar el resto de campos")
    @ApiResponse(responseCode = "204", description = "Disponibilidad cambiada con éxito")
    public ResponseEntity<Void> cambiarDisponibilidad(@PathVariable Long id, @RequestParam boolean disponible) {
        productoService.cambiarDisponibilidad(id, disponible);
        return ResponseEntity.noContent().build();
    }

    // 8. Buscar por nombre
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MESERO', 'CLIENTE')")
    @Operation(summary = "Buscar productos por nombre", description = "Filtro rápido sin paginación para autocompletados en la barra de búsqueda")
    @ApiResponse(responseCode = "200", description = "Resultados coincidentes")
    public ResponseEntity<List<ProductoResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    // 9. Actualización masiva de precios
    @PatchMapping("/precios-masivo")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Ajuste masivo de precios", description = "Afecta el precio de todo el catálogo multiplicándolo bajo un porcentaje flotante (Ej: 10 aumenta un 10%)")
    @ApiResponse(responseCode = "204", description = "Actualización masiva ejecutada de manera exitosa")
    public ResponseEntity<Void> actualizarPreciosMasivo(@RequestParam double porcentaje) {
        productoService.actualizarPreciosMasivo(porcentaje);
        return ResponseEntity.noContent().build();
    }

    // 10. Eliminar producto
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Eliminar producto (Baja lógica)", description = "Aplica soft delete al producto y lo remueve de la vista comercial inmediatamente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto dado de baja con éxito"),
            @ApiResponse(responseCode = "404", description = "El ID ingresado no coincide con un producto activo")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
