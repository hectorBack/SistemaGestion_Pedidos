package com.Sistema.Backend.Reportes.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class VentasPorCategoriaDTO {

    private String categoria;
    private long cantidadVendida;
    private BigDecimal totalRecaudado;
}
