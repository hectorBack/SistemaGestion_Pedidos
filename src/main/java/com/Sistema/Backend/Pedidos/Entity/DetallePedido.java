package com.Sistema.Backend.Pedidos.Entity;

import com.Sistema.Backend.Productos.Entity.Producto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_pedidos")
@Getter
@Setter
@ToString
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación ManyToOne con Pedido
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // Relación ManyToOne con Producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    // Aquí guardamos: "Sin cebolla", "Sin tomate", etc.
    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "enviado_a_cocina", nullable = false, columnDefinition = "boolean default false")
    private boolean enviadoACocina = false; // Por defecto los nuevos items entran en false
}
