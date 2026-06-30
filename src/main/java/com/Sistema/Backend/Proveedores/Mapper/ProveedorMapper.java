package com.Sistema.Backend.Proveedores.Mapper;

import com.Sistema.Backend.Proveedores.Dto.Request.ProveedorRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.ProveedorResponseDTO;
import com.Sistema.Backend.Proveedores.Entity.Proveedor;
import org.springframework.stereotype.Component;

@Component
public class ProveedorMapper {

    // Convierte de Request DTO a Entidad JPA (Para crear/actualizar)
    public Proveedor toEntity(ProveedorRequestDTO dto) {
        if (dto == null) return null;

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(dto.getNombre());
        proveedor.setContacto(dto.getContacto());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        if (dto.getActivo() != null) {
            proveedor.setActivo(dto.getActivo());
        }
        return proveedor;
    }

    // Convierte de Entidad JPA a Response DTO (Para enviar al Frontend)
    public ProveedorResponseDTO toResponseDTO(Proveedor proveedor) {
        if (proveedor == null) return null;

        ProveedorResponseDTO dto = new ProveedorResponseDTO();
        dto.setId(proveedor.getId());
        dto.setNombre(proveedor.getNombre());
        dto.setContacto(proveedor.getContacto());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEmail(proveedor.getEmail());
        dto.setActivo(proveedor.getActivo());
        return dto;
    }

    // Método utilitario para actualizar una entidad existente sin perder su ID
    public void updateEntityFromDTO(ProveedorRequestDTO dto, Proveedor proveedor) {
        if (dto == null || proveedor == null) return;

        proveedor.setNombre(dto.getNombre());
        proveedor.setContacto(dto.getContacto());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        if (dto.getActivo() != null) {
            proveedor.setActivo(dto.getActivo());
        }
    }
}
