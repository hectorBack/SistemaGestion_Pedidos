package com.Sistema.Backend.Pedidos.Dto.Response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemResponseDTO {

    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private String notas;
}
