package com.Sistema.Backend.Mesas.Services;

import com.Sistema.Backend.Mesas.Dto.Request.CambioEstadoRequestDTO;
import com.Sistema.Backend.Mesas.Dto.Request.MesaRequestDTO;
import com.Sistema.Backend.Mesas.Dto.Response.MesaResponseDTO;
import com.Sistema.Backend.Mesas.Entity.EstadoMesa;
import com.Sistema.Backend.Pedidos.Dto.Request.ComandaMesaRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Request.PedidoRequestDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface MesaService {

    List<MesaResponseDTO> obtenerTodas();
    List<MesaResponseDTO> obtenerPorEstado(EstadoMesa estado);
    MesaResponseDTO obtenerPorId(Long id);
    MesaResponseDTO crearMesa(MesaRequestDTO request);
    MesaResponseDTO actualizarMesa(Long id, MesaRequestDTO request);

    // Métodos operativos con reglas de negocio específicas
    MesaResponseDTO abrirMesa(Long id, ComandaMesaRequestDTO comandaRequest, Long meseroId, Authentication authentication);
    MesaResponseDTO cambiarEstadoRapido(Long id, CambioEstadoRequestDTO request);
    MesaResponseDTO reservarMesa(Long id, String notasReserva);
}
