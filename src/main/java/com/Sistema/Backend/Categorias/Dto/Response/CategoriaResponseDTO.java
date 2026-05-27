package com.Sistema.Backend.Categorias.Dto.Response;

import lombok.Data;

@Data
public class CategoriaResponseDTO {
    private Long id;
    private String nombre;
    private Boolean activo;
}
