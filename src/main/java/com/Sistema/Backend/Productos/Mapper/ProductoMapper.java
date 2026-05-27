package com.Sistema.Backend.Productos.Mapper;

import com.Sistema.Backend.Productos.Dto.Request.ProductoRequestDTO;
import com.Sistema.Backend.Productos.Dto.Response.ProductoResponseDTO;
import com.Sistema.Backend.Productos.Entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public ProductoResponseDTO toResponseDTO(Producto producto) {
        if (producto == null) return null;
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setDisponible(producto.isDisponible());
        dto.setCategoria(producto.getCategoria());
        dto.setUrlImagen(producto.getUrlImagen());
        return dto;
    }

    public Producto toEntity(ProductoRequestDTO dto) {
        if (dto == null) return null;
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setDisponible(dto.isDisponible());
        producto.setCategoria(dto.getCategoria());
        producto.setUrlImagen(dto.getUrlImagen());
        return producto;
    }
}
