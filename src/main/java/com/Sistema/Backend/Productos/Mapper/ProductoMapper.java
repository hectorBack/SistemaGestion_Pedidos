package com.Sistema.Backend.Productos.Mapper;

import com.Sistema.Backend.Categorias.Entity.Categoria;
import com.Sistema.Backend.Categorias.Repository.CategoriaRepository;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Productos.Dto.Request.ProductoRequestDTO;
import com.Sistema.Backend.Productos.Dto.Response.ProductoResponseDTO;
import com.Sistema.Backend.Productos.Entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    private final CategoriaRepository categoriaRepository;

    public ProductoMapper(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public ProductoResponseDTO toResponseDTO(Producto producto) {
        if (producto == null) return null;

        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setDisponible(producto.isDisponible());

        if (producto.getCategoria() != null){
            dto.setCategoriaId(producto.getCategoria().getId());
            dto.setNombreCategoria(producto.getCategoria().getNombre());
        }

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

        if (dto.getCategoriaId() != null){
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " + dto.getCategoriaId()));
            producto.setCategoria(categoria);
        }

        producto.setUrlImagen(dto.getUrlImagen());
        return producto;
    }
}
