package com.Sistema.Backend.Proveedores.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProveedorRequestDTO {

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El contacto no puede superar los 100 caracteres")
    private String contacto;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String telefono;

    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    private String email;

    private Boolean activo = true;
}
