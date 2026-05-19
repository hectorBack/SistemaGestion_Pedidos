package com.Sistema.Backend.Services;

import com.Sistema.Backend.Dto.Request.PromocionRequestDTO;
import com.Sistema.Backend.Dto.Response.PromocionResponseDTO;

import java.util.List;

public interface PromocionService {

    PromocionResponseDTO crearPromocion(PromocionRequestDTO dto);
    List<PromocionResponseDTO> listarPromocionesVigentes();
    List<PromocionResponseDTO> listarTodas();
    void desactivarPromocion(Long id);
}
