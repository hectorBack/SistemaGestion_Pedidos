package com.Sistema.Backend.Categorias.Dto.Response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaResponseDTO {
    private Long id;
    private String nombre;
    private Boolean activo;
    private Integer cantidadProductos;
    private Integer orden;
}
