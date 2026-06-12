package com.Sistema.Backend.Promociones.Dto.Response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromocionResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String tipoDescuento;
    private BigDecimal valor;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private boolean activa;
    private Long productoId; // Solo enviamos el ID del producto para el frontend
    private String nombreProducto;
    private BigDecimal precioProducto;
}
