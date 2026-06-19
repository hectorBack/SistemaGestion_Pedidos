package com.Sistema.Backend.Promociones.Entity;

import com.Sistema.Backend.Productos.Entity.Producto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promociones")
@Setter
@Getter
@ToString
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // Ej: "Martes de 2x1" o "Descuento de Apertura"

    private String descripcion;

    @Column(name = "tipo_descuento", nullable = false)
    private String tipoDescuento; // "PORCENTAJE" (ej: 15%) o "FIJO" (ej: $50 pesos)

    @Column(nullable = false)
    private BigDecimal valor; // El 15.00 o el 50.00 según el tipo

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    private boolean activa = true;

    // Relación opcional: Si la promo aplica a un producto específico.
    // Si es null, puede aplicar a todo el carrito o ser un cupón general.
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = true)
    private Producto producto;
}
