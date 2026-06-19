package com.Sistema.Backend.Categorias.Dto;

import com.Sistema.Backend.Productos.Dto.Response.ProductoResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MenuCategoriaDTO {

    private Long id;
    private String nombre;
    private Integer orden;
    private List<ProductoResponseDTO> productos;
}
