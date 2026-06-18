package com.Sistema.Backend.Categorias.Dto.Request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CategoriaRequestDTO {
    private String nombre;
    private Boolean activo;
}
