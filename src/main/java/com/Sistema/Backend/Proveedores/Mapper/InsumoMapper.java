package com.Sistema.Backend.Proveedores.Mapper;

import com.Sistema.Backend.Proveedores.Dto.Request.InsumoRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.InsumoResponseDTO;
import com.Sistema.Backend.Proveedores.Entity.Insumo;
import org.springframework.stereotype.Component;

@Component
public class InsumoMapper {

    // Convierte de RequestDTO a la Entidad (Para crear)
    public Insumo toEntity(InsumoRequestDTO dto) {
        if (dto == null) return null;

        Insumo insumo = new Insumo();
        insumo.setNombre(dto.getNombre());
        insumo.setUnidadMedida(dto.getUnidadMedida());
        insumo.setDescripcion(dto.getDescripcion());
        insumo.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return insumo;
    }

    // Convierte de Entidad a ResponseDTO (Para enviar a la UI)
    public InsumoResponseDTO toResponseDTO(Insumo insumo) {
        if (insumo == null) return null;

        InsumoResponseDTO dto = new InsumoResponseDTO();
        dto.setId(insumo.getId());
        dto.setNombre(insumo.getNombre());
        dto.setUnidadMedida(insumo.getUnidadMedida());
        dto.setDescripcion(insumo.getDescripcion());
        dto.setActivo(insumo.getActivo());
        return dto;
    }

    // Actualiza una entidad existente con los datos de un RequestDTO (Para editar)
    public void updateEntityFromDTO(InsumoRequestDTO dto, Insumo insumo) {
        if (dto == null || insumo == null) return;

        insumo.setNombre(dto.getNombre());
        insumo.setUnidadMedida(dto.getUnidadMedida());
        insumo.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            insumo.setActivo(dto.getActivo());
        }
    }
}
