package com.Sistema.Backend.Services.Impl;

import com.Sistema.Backend.Dto.Request.PromocionRequestDTO;
import com.Sistema.Backend.Dto.Response.PromocionResponseDTO;
import com.Sistema.Backend.Entity.Promocion;
import com.Sistema.Backend.Mapper.PromocionMapper;
import com.Sistema.Backend.Repository.PromocionRepository;
import com.Sistema.Backend.Services.PromocionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromocionServiceImpl implements PromocionService {

    private final PromocionRepository promocionRepository;
    private final PromocionMapper promocionMapper;

    public PromocionServiceImpl(PromocionRepository promocionRepository, PromocionMapper promocionMapper) {
        this.promocionRepository = promocionRepository;
        this.promocionMapper = promocionMapper;
    }

    @Override
    @Transactional
    public PromocionResponseDTO crearPromocion(PromocionRequestDTO dto) {
        // 1. Convertimos DTO a Entidad
        Promocion promocion = promocionMapper.toEntity(dto);

        // 2. Guardamos en PostgreSQL
        Promocion promocionGuardada = promocionRepository.save(promocion);

        // 3. Retornamos la respuesta mapeada
        return promocionMapper.toResponseDTO(promocionGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> listarPromocionesVigentes() {
        return promocionRepository.findPromocionesVigentes().stream()
                .map(promocionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromocionResponseDTO> listarTodas() {
        return promocionRepository.findAll().stream()
                .map(promocionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void desactivarPromocion(Long id) {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada con ID: " + id));
        promocion.setActiva(false);
        promocionRepository.save(promocion);
    }
}
