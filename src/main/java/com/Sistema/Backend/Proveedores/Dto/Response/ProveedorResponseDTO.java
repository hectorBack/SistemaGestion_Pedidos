package com.Sistema.Backend.Proveedores.Dto.Response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProveedorResponseDTO {

    private Long id;
    private String nombre;
    private String contacto;
    private String telefono;
    private String email;
    private Boolean activo;
}
