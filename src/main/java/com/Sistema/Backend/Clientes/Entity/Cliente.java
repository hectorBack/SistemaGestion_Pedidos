package com.Sistema.Backend.Clientes.Entity;

import com.Sistema.Backend.Usuarios.Entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación limpia con la tabla de credenciales/usuarios
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // Atributos específicos que crecerán con el negocio
    @Column(nullable = false, length = 100, columnDefinition = "TEXT")
    private String nombreCompleto;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String direccionEntrega;

    @Column(nullable = false)
    private boolean activo = true;
}
