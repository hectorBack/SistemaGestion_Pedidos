package com.Sistema.Backend.Categorias.Entity;

import com.Sistema.Backend.Productos.Entity.Producto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "productos")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    // Relación de uno a muchos: Una categoría tiene muchos productos
    // mappedBy apunta al atributo 'categoria' en la entidad Producto
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Producto> productos;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Categoria)) return false;
        Categoria categoria = (Categoria) o;
        // Comparamos directamente usando el atributo .id de ambas instancias
        return id != null && id.equals(categoria.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
