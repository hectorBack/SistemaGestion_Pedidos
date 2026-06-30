package com.Sistema.Backend.Proveedores.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 100)
    private String contacto; // Nombre de la persona que te atiende (ej: Carlos Mendoza)

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @NotNull
    @Column(nullable = false)
    private Boolean activo = true;

}
