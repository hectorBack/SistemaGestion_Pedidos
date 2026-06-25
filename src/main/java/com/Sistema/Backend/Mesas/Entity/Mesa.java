package com.Sistema.Backend.Mesas.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "mesas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El número o identificador de la mesa es obligatorio")
    @Size(max = 20)
    @Column(unique = true, nullable = false, length = 20)
    private String numero; // Ej: "Mesa 1", "Barra 2"

    @NotNull(message = "La capacidad de comensales es obligatoria")
    @Min(value = 1, message = "La capacidad mínima debe ser de al menos 1 persona")
    @Column(nullable = false)
    private Integer capacidad;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMesa estado = EstadoMesa.LIBRE;

    // Relaciones u observaciones opcionales según el flujo operativo
    @Column(name = "pedido_id")
    private Long pedidoId; // Vinculado cuando el estado sea OCUPADA

    @Column(name = "mesero_id")
    private Long meseroId; // Vinculado cuando el estado sea OCUPADA

    @Column(name = "notas_reserva", length = 255)
    private String notasReserva; // Vinculado cuando el estado sea RESERVADA
}
