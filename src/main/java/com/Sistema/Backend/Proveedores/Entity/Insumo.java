package com.Sistema.Backend.Proveedores.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "insumos")
@Getter
@Setter
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del insumo es obligatorio")
    @Column(name = "nombre", length = 150, columnDefinition = "VARCHAR(150)", nullable = false)
    private String nombre; // Ej: "Jamón", "Queso Manchego", "Papas Fritas"

    @NotBlank(message = "La unidad de medida interna es obligatoria")
    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida; // Ej: "Gramo", "Pieza", "Bolsa" (Cómo lo gastas en la cocina)

    @Column(length = 255)
    private String descripcion; // Opcional, por si quieren anotar la marca o notas extras

    @NotNull
    @Column(nullable = false)
    private Boolean activo = true; // 🌟 Tu atributo de Estado (Activo/Inactivo o Vigente)
}
