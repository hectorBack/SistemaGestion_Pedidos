package com.Sistema.Backend.Mesas.Dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MesaRequestDTO {

    @NotBlank(message = "El número de la mesa no puede estar vacío")
    @Size(max = 20, message = "El número no puede exceder los 20 caracteres")
    private String numero;

    @NotNull(message = "La capacidad es requerida")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;
}
