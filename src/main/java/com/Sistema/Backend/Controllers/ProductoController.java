package com.Sistema.Backend.Controllers;

import com.Sistema.Backend.Dto.Request.ProductoRequestDTO;
import com.Sistema.Backend.Dto.Response.ProductoResponseDTO;
import com.Sistema.Backend.Entity.Producto;
import com.Sistema.Backend.Services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // 1. Crear producto (Panel Admin)
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO request) {
        return new ResponseEntity<>(productoService.crear(request), HttpStatus.CREATED);
    }

    // 2. Listar Paginado (Panel Admin)
    @GetMapping
    public ResponseEntity<Page<Producto>> listarProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean disponible,
            @org.springframework.data.web.PageableDefault(size = 8) Pageable pageable) {

        Page<Producto> productos = productoService.listarPaginado(nombre, categoria, disponible, pageable);
        return ResponseEntity.ok(productos);
    }

    // 3. Listar solo disponibles (Para el cliente/link)
    @GetMapping("/disponibles")
    public ResponseEntity<List<ProductoResponseDTO>> listarDisponibles() {
        return ResponseEntity.ok(productoService.listarDisponibles());
    }

    // 4. Menú agrupado por categorías (¡Ideal para el Link del Cliente!)
    @GetMapping("/menu")
    public ResponseEntity<Map<String, List<ProductoResponseDTO>>> obtenerMenuPorCategorias() {
        return ResponseEntity.ok(productoService.listarMenuPorCategoria());
    }

    // 5. Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // 6. Actualizar producto completo
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    // 7. Cambiar solo disponibilidad (Switch rápido en Admin)
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<Void> cambiarDisponibilidad(@PathVariable Long id, @RequestParam boolean disponible) {
        productoService.cambiarDisponibilidad(id, disponible);
        return ResponseEntity.noContent().build();
    }

    // 8. Buscar por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    // 9. Actualización masiva de precios
    @PatchMapping("/precios-masivo")
    public ResponseEntity<Void> actualizarPreciosMasivo(@RequestParam double porcentaje) {
        productoService.actualizarPreciosMasivo(porcentaje);
        return ResponseEntity.noContent().build();
    }

    // 10. Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
