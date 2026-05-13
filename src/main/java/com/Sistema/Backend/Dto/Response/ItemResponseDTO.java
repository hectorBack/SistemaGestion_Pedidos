package com.Sistema.Backend.Dto.Response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemResponseDTO {

    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private String notas;
}
