package com.Sistema.Backend.Productos.Entity;

import com.Sistema.Backend.Categorias.Entity.Categoria;
import com.Sistema.Backend.Promociones.Entity.Promocion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Getter
@Setter
@ToString
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precio;

    private boolean disponible = true;

    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Categoria categoria;

    @Column(name = "url_imagen")
    private String urlImagen;

    // 🌟 LA SOLUCIÓN IDEAL: Lista de sabores/variantes sin crear otra entidad
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "producto_sabores", joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "sabor")
    private List<String> sabores = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_id") // Clave foránea si el producto apunta a su promoción activa
    private Promocion promocion;
}
