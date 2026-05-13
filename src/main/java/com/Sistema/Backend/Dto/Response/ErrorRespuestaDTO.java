package com.Sistema.Backend.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorRespuestaDTO {

    private LocalDateTime timestamp;
    private String mensaje;
    private String detalles;
}
