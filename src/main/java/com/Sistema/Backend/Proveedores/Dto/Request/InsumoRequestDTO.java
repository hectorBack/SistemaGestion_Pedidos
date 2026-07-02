package com.Sistema.Backend.Proveedores.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsumoRequestDTO {

    @NotBlank(message = "El nombre del insumo es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombre; // Ej: "Jamón", "Queso Manchego"

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 50, message = "La unidad de medida no puede superar los 50 caracteres")
    private String unidadMedida; // Ej: "Gramo", "Pieza", "Kilo", "Bolsa"

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcion; // Notas opcionales o marca

    private Boolean activo = true; // El atributo de Estado que me pediste
}
