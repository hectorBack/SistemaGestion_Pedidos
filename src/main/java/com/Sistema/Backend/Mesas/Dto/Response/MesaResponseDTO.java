package com.Sistema.Backend.Mesas.Dto.Response;

import com.Sistema.Backend.Mesas.Entity.EstadoMesa;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MesaResponseDTO {

    private Long id;
    private String numero;
    private Integer capacidad;
    private EstadoMesa estado;
    private Long pedidoId;
    private Long meseroId;
    private String notasReserva;
}
