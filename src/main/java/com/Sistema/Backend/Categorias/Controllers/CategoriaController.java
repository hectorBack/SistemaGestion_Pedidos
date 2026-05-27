package com.Sistema.Backend.Categorias.Controllers;

import com.Sistema.Backend.Categorias.Dto.Request.CategoriaRequestDTO;
import com.Sistema.Backend.Categorias.Dto.Response.CategoriaResponseDTO;
import com.Sistema.Backend.Categorias.Services.CategoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<Page<CategoriaResponseDTO>> listarPaginado(
            @RequestParam(required = false) String nombre,
            @PageableDefault(size = 8) Pageable pageable) {

        Page<CategoriaResponseDTO> pagina = categoriaService.listarPaginado(nombre, pageable);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/todas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodas() {
        List<CategoriaResponseDTO> lista = categoriaService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(@PathVariable Long id) {
        CategoriaResponseDTO dto = categoriaService.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(@RequestBody CategoriaRequestDTO dto) {
        CategoriaResponseDTO nueva = categoriaService.crear(dto);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody CategoriaRequestDTO dto) {
        CategoriaResponseDTO actualizada = categoriaService.actualizar(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Realiza el Soft Delete cambiando el estado 'activo' a false en la base de datos.
     * Devuelve un código HTTP 204 No Content para indicar éxito sin cuerpo de respuesta.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
