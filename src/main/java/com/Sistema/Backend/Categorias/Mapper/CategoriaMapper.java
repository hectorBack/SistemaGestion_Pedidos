package com.Sistema.Backend.Categorias.Mapper;

import com.Sistema.Backend.Categorias.Dto.Request.CategoriaRequestDTO;
import com.Sistema.Backend.Categorias.Dto.Response.CategoriaResponseDTO;
import com.Sistema.Backend.Categorias.Entity.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public CategoriaResponseDTO toResponseDTO(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setActivo(categoria.getActivo());
        dto.setOrden(categoria.getOrden());

        // Mapea la cantidad de productos asociados de forma dinámica
        if (categoria.getProductos() != null) {
            dto.setCantidadProductos(categoria.getProductos().size());
        } else {
            dto.setCantidadProductos(0);
        }

        return dto;
    }

    public Categoria toEntity(CategoriaRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());

        // Al crearse por primera vez, el DTO puede no traer el estado,
        // así que nos aseguramos de que nazca activa por defecto.
        categoria.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return categoria;
    }

    public void updateEntityFromDTO(CategoriaRequestDTO dto, Categoria categoria) {
        if (dto == null || categoria == null) {
            return;
        }

        if (dto.getNombre() != null) {
            categoria.setNombre(dto.getNombre());
        }

        if (dto.getActivo() != null) {
            categoria.setActivo(dto.getActivo());
        }
    }
}
