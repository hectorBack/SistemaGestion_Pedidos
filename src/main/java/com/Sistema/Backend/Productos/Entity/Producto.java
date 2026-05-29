package com.Sistema.Backend.Productos.Entity;

import com.Sistema.Backend.Categorias.Entity.Categoria;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Data
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
}
