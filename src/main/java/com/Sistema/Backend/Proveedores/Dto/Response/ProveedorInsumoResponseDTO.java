package com.Sistema.Backend.Proveedores.Dto.Response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ProveedorInsumoResponseDTO {

    private Long id;
    private Long proveedorId;
    private String proveedorNombre; // 🌟 Muy útil para mostrar en la UI
    private Long insumoId;
    private String insumoNombre;
    private BigDecimal precioCompra;
    private String unidadCompra;
    private Integer factorConversion;
    private Boolean activo;
}
