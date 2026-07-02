package com.Sistema.Backend.Proveedores.Dto.Response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsumoResponseDTO {

    private Long id;
    private String nombre;
    private String unidadMedida;
    private String descripcion;
    private Boolean activo; // Estado (Activo/Inactivo)
}
