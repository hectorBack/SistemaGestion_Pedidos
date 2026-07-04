package com.Sistema.Backend.Productos.Dto.Response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private boolean disponible;
    private Boolean activo;
    private Long categoriaId;
    private String nombreCategoria;
    private String urlImagen;
    private List<String> sabores;
}