package com.Sistema.Backend.Reportes.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class VentasPorCategoriaDTO {

    private String categoria;
    private long cantidadVendida;
    private BigDecimal totalRecaudado;
}
