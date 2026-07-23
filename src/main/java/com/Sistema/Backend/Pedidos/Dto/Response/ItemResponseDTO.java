package com.Sistema.Backend.Pedidos.Dto.Response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ItemResponseDTO {

    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private String notas;
    private List<String> sabores;

    // Campo para promociones específicas de este producto
    private String promocionNombre;
}
