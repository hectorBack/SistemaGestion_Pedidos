package com.Sistema.Backend.Proveedores.Dto.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProveedorInsumoRequestDTO {

    @NotNull(message = "El ID del proveedor es obligatorio")
    private Long proveedorId;

    @NotNull(message = "El ID del insumo es obligatorio")
    private Long insumoId;

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    private BigDecimal precioCompra;

    @NotBlank(message = "La unidad de compra es obligatoria")
    private String unidadCompra;

    @NotNull(message = "El factor de conversión es obligatorio")
    @Min(value = 1, message = "El factor de conversión mínimo es 1")
    private Integer factorConversion;

    private Boolean activo = true;
}
