package com.Sistema.Backend.Promociones.Services;

import com.Sistema.Backend.Promociones.Dto.Request.PromocionRequestDTO;
import com.Sistema.Backend.Promociones.Dto.Response.PromocionResponseDTO;
import com.Sistema.Backend.Promociones.Entity.Promocion;
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
    Page<PromocionResponseDTO> listarPaginado(String nombre, Boolean activa, Pageable pageable);
}
