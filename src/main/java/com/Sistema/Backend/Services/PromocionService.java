package com.Sistema.Backend.Services;

import com.Sistema.Backend.Dto.Request.PromocionRequestDTO;
import com.Sistema.Backend.Dto.Response.PromocionResponseDTO;
import com.Sistema.Backend.Entity.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PromocionService {

    PromocionResponseDTO crearPromocion(PromocionRequestDTO dto);
    List<PromocionResponseDTO> listarPromocionesVigentes();
    List<PromocionResponseDTO> listarTodas();
    void desactivarPromocion(Long id);
    PromocionResponseDTO actualizarPromocion(Long id, PromocionRequestDTO request);

    /**
     * Listar paginado
     */
    Page<Promocion> listarPaginado(String nombre, Pageable pageable);
}
