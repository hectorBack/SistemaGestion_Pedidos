package com.Sistema.Backend.Mesas.Mapper;

import com.Sistema.Backend.Mesas.Dto.Request.MesaRequestDTO;
import com.Sistema.Backend.Mesas.Dto.Response.MesaResponseDTO;
import com.Sistema.Backend.Mesas.Entity.EstadoMesa;
import com.Sistema.Backend.Mesas.Entity.Mesa;
import org.springframework.stereotype.Component;

@Component
public class MesaMapper {

    public MesaResponseDTO toResponse(Mesa mesa) {
        if (mesa == null) return null;

        return new MesaResponseDTO(
                mesa.getId(),
                mesa.getNumero(),
                mesa.getCapacidad(),
                mesa.getEstado(),
                mesa.getPedidoId(),
                mesa.getMeseroId(),
                mesa.getNotasReserva()
        );
    }

    public Mesa toEntity(MesaRequestDTO request) {
        if (request == null) return null;

        return Mesa.builder()
                .numero(request.getNumero())
                .capacidad(request.getCapacidad())
                .estado(EstadoMesa.LIBRE)
                .build();
    }
}
