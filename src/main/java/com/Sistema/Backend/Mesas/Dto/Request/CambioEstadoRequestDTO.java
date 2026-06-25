package com.Sistema.Backend.Mesas.Dto.Request;

import com.Sistema.Backend.Mesas.Entity.EstadoMesa;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambioEstadoRequestDTO {

    @NotNull(message = "El nuevo estado es requerido")
    private EstadoMesa nuevoEstado;
}
