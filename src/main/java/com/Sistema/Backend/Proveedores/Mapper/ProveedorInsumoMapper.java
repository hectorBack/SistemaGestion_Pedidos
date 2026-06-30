package com.Sistema.Backend.Proveedores.Mapper;

import com.Sistema.Backend.Categorias.Entity.ProveedorInsumo;
import com.Sistema.Backend.Proveedores.Dto.Request.ProveedorInsumoRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.ProveedorInsumoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ProveedorInsumoMapper {

    // Convierte de Request DTO a Entidad (Nota: El proveedor se debe setear en el Service)
    public ProveedorInsumo toEntity(ProveedorInsumoRequestDTO dto) {
        if (dto == null) return null;

        ProveedorInsumo proveedorInsumo = new ProveedorInsumo();
        proveedorInsumo.setInsumoId(dto.getInsumoId());
        proveedorInsumo.setPrecioCompra(dto.getPrecioCompra());
        proveedorInsumo.setUnidadCompra(dto.getUnidadCompra());
        proveedorInsumo.setFactorConversion(dto.getFactorConversion());
        if (dto.getActivo() != null) {
            proveedorInsumo.setActivo(dto.getActivo());
        }
        return proveedorInsumo;
    }

    // Convierte de Entidad a Response DTO inyectando de forma segura datos de la relación Lazy
    public ProveedorInsumoResponseDTO toResponseDTO(ProveedorInsumo entity) {
        if (entity == null) return null;

        ProveedorInsumoResponseDTO dto = new ProveedorInsumoResponseDTO();
        dto.setId(entity.getId());
        dto.setInsumoId(entity.getInsumoId());
        dto.setPrecioCompra(entity.getPrecioCompra());
        dto.setUnidadCompra(entity.getUnidadCompra());
        dto.setFactorConversion(entity.getFactorConversion());
        dto.setActivo(entity.getActivo());

        // 🌟 Extraemos de forma segura los datos de la relación ManyToOne
        if (entity.getProveedor() != null) {
            dto.setProveedorId(entity.getProveedor().getId());
            dto.setProveedorNombre(entity.getProveedor().getNombre());
        }

        return dto;
    }

    // Método utilitario para actualizar los costos y factores de un registro existente
    public void updateEntityFromDTO(ProveedorInsumoRequestDTO dto, ProveedorInsumo entity) {
        if (dto == null || entity == null) return;

        entity.setInsumoId(dto.getInsumoId());
        entity.setPrecioCompra(dto.getPrecioCompra());
        entity.setUnidadCompra(dto.getUnidadCompra());
        entity.setFactorConversion(dto.getFactorConversion());
        if (dto.getActivo() != null) {
            entity.setActivo(dto.getActivo());
        }
    }
}
