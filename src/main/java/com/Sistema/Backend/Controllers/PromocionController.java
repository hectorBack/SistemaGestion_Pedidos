package com.Sistema.Backend.Controllers;

import com.Sistema.Backend.Dto.Request.ProductoRequestDTO;
import com.Sistema.Backend.Dto.Request.PromocionRequestDTO;
import com.Sistema.Backend.Dto.Response.PromocionResponseDTO;
import com.Sistema.Backend.Services.PromocionService;
import jakarta.validation.Valid;
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

    @GetMapping
    public List<PromocionResponseDTO> getTodas() {
        return promocionService.listarTodas();
    }

    @PatchMapping("/{id}/desactivar")
    public void desactivar(@PathVariable Long id) {
        promocionService.desactivarPromocion(id);
    }
}
