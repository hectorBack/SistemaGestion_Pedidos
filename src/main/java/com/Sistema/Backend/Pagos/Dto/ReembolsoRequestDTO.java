package com.Sistema.Backend.Pagos.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReembolsoRequestDTO {

    @NotBlank(message = "El motivo del reembolso es obligatorio")
    @Size(max = 255, message = "El motivo no puede exceder los 255 caracteres")
    private String motivo;
}
