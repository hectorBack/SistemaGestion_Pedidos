package com.Sistema.Backend.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromocionRequestDTO {

    @NotBlank(message = "El nombre de la promoción no puede estar vacío")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "El tipo de descuento es obligatorio (PORCENTAJE o FIJO)")
    private String tipoDescuento;

    @NotNull(message = "El valor del descuento es obligatorio")
    @Positive(message = "El valor del descuento debe ser mayor a cero")
    private BigDecimal valor;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private boolean activa = true;

    // ID del producto al que aplica (puede ser null si es una promo global)
    private Long productoId;
}
