package com.Sistema.Backend.Categorias.Entity;

import com.Sistema.Backend.Proveedores.Entity.Proveedor;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Setter
@Getter
@Table(name = "proveedores_insumos")
public class ProveedorInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El proveedor es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @NotNull(message = "El ID del insumo es obligatorio")
    @Column(name = "insumo_id", nullable = false)
    private Long insumoId; // ID del insumo de tu módulo de Inventario

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCompra; // Precio total del empaque/paquete (ej: 850.00)

    @NotBlank(message = "La unidad de compra es obligatoria")
    @Column(nullable = false, length = 50)
    private String unidadCompra; // Cómo te lo venden (ej: "PAQUETE", "CAJA", "KILO")

    @NotNull(message = "El factor de conversión es obligatorio")
    @Min(value = 1, message = "El factor de conversión mínimo es 1")
    @Column(nullable = false)
    private Integer factorConversion;
    // 💡 Ej: Si el paquete trae 10 cortes de Arrachera, aquí pones 10.
    // Si el Queso Manchego se compra por Kilo y se gasta por Kilo, aquí pones 1.

    @NotNull
    @Column(nullable = false)
    private Boolean activo = true;
}
