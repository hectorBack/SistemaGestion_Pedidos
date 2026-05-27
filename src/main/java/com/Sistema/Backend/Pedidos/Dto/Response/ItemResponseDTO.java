package com.Sistema.Backend.Pedidos.Dto.Response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemResponseDTO {

    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private String notas;
}
