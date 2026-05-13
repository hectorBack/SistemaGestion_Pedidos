package com.Sistema.Backend.Dto.Response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private boolean disponible;
    private String categoria;
    private String urlImagen;
}