package com.Sistema.Backend.Promociones.Controllers;

import com.Sistema.Backend.Promociones.Dto.Request.PromocionRequestDTO;
import com.Sistema.Backend.Promociones.Dto.Response.PromocionResponseDTO;
import com.Sistema.Backend.Promociones.Entity.Promocion;
import com.Sistema.Backend.Promociones.Services.PromocionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")
public class PromocionController {

    private final PromocionService promocionService;

    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    //Listar Paginado
    @GetMapping
    public ResponseEntity<Page<PromocionResponseDTO>> listarPromociones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activa
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PromocionResponseDTO> promociones = promocionService.listarPaginado(nombre, activa, pageable);
        return ResponseEntity.ok(promociones);
    }

    @PostMapping
    public PromocionResponseDTO crear(@Valid @RequestBody PromocionRequestDTO dto) {
        return promocionService.crearPromocion(dto);
    }

    @GetMapping("/vigentes")
    public List<PromocionResponseDTO> getPromocionesVigentes() {
        return promocionService.listarPromocionesVigentes();
    }

    // Actualizar promocion completo
    @PutMapping("/{id}")
        public ResponseEntity<PromocionResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PromocionRequestDTO request){
        return ResponseEntity.ok(promocionService.actualizarPromocion(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public void desactivar(@PathVariable Long id) {
        promocionService.desactivarPromocion(id);
    }
}
