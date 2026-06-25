package com.Sistema.Backend.Mesas.Services.Impl;

import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Mesas.Dto.Request.CambioEstadoRequestDTO;
import com.Sistema.Backend.Mesas.Dto.Request.MesaRequestDTO;
import com.Sistema.Backend.Mesas.Dto.Response.MesaResponseDTO;
import com.Sistema.Backend.Mesas.Entity.EstadoMesa;
import com.Sistema.Backend.Mesas.Entity.Mesa;
import com.Sistema.Backend.Mesas.Exception.BusinessException;
import com.Sistema.Backend.Mesas.Mapper.MesaMapper;
import com.Sistema.Backend.Mesas.Repository.MesaRepository;
import com.Sistema.Backend.Mesas.Services.MesaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MesaServiceImpl implements MesaService {

    private final MesaRepository mesaRepository;
    private final MesaMapper mesaMapper;

    public MesaServiceImpl(MesaRepository mesaRepository, MesaMapper mesaMapper) {
        this.mesaRepository = mesaRepository;
        this.mesaMapper = mesaMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MesaResponseDTO> obtenerTodas() {
        log.debug("Solicitando el listado completo de mesas del salón");
        return mesaRepository.findAll().stream()
                .map(mesaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MesaResponseDTO> obtenerPorEstado(EstadoMesa estado) {
        log.debug("Filtrando mesas por estado: {}", estado);
        return mesaRepository.findByEstado(estado).stream()
                .map(mesaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MesaResponseDTO obtenerPorId(Long id) {
        log.debug("Buscando mesa con ID: {}", id);
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada con ID: " + id));
        return mesaMapper.toResponse(mesa);
    }

    @Override
    @Transactional
    public MesaResponseDTO crearMesa(MesaRequestDTO request) {
        log.info("Iniciando registro de nueva mesa: {}", request.getNumero());

        if (mesaRepository.existsByNumero(request.getNumero())) {
            log.error("Error al crear mesa: El identificador '{}' ya existe", request.getNumero());
            throw new BusinessException("Ya existe una mesa registrada con el número: " + request.getNumero());
        }

        Mesa nuevaMesa = mesaMapper.toEntity(request);
        Mesa mesaGuardada = mesaRepository.save(nuevaMesa);

        log.info("Mesa '{}' creada exitosamente con ID: {}", mesaGuardada.getNumero(), mesaGuardada.getId());
        return mesaMapper.toResponse(mesaGuardada);
    }

    @Override
    @Transactional
    public MesaResponseDTO actualizarMesa(Long id, MesaRequestDTO request) {
        log.info("Actualizando propiedades de la mesa ID: {}", id);

        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));

        if (!mesa.getNumero().equalsIgnoreCase(request.getNumero()) && mesaRepository.existsByNumero(request.getNumero())) {
            log.error("Conflictos de actualización: El número '{}' ya pertenece a otra mesa", request.getNumero());
            throw new BusinessException("El número de mesa ya está en uso");
        }

        mesa.setNumero(request.getNumero());
        mesa.setCapacidad(request.getCapacidad());

        return mesaMapper.toResponse(mesaRepository.save(mesa));
    }

    @Override
    @Transactional
    public MesaResponseDTO abrirMesa(Long id, Long pedidoId, Long meseroId) {
        log.info("Intento de apertura operativa para mesa ID: {}. Pedido: {}, Mesero: {}", id, pedidoId, meseroId);

        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));

        if (mesa.getEstado() == EstadoMesa.OCUPADA) {
            log.warn("Transición denegada: La mesa ID: {} ya se encuentra ocupada", id);
            throw new BusinessException("La mesa ya se encuentra ocupada con otra orden activa");
        }

        if (pedidoId == null || meseroId == null) {
            log.error("Transición denegada: Faltan parámetros requeridos para la comanda");
            throw new BusinessException("Para abrir una mesa es obligatorio asignar un pedido y un mesero responsable");
        }

        mesa.setEstado(EstadoMesa.OCUPADA);
        mesa.setPedidoId(pedidoId);
        mesa.setMeseroId(meseroId);
        mesa.setNotasReserva(null);

        log.info("Mesa '{}' abierta correctamente bajo la orden '{}'", mesa.getNumero(), pedidoId);
        return mesaMapper.toResponse(mesaRepository.save(mesa));
    }

    @Override
    @Transactional
    public MesaResponseDTO reservarMesa(Long id, String notasReserva) {
        log.info("Registrando reservación para la mesa ID: {}", id);

        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));

        if (mesa.getEstado() != EstadoMesa.LIBRE) {
            log.warn("Reservación rechazada: La mesa '{}' está en estado {}", mesa.getNumero(), mesa.getEstado());
            throw new BusinessException("Solo se pueden reservar mesas que estén en estado LIBRE");
        }

        if (notasReserva == null || notasReserva.isBlank()) {
            throw new BusinessException("Es necesario especificar las notas de la reservación");
        }

        mesa.setEstado(EstadoMesa.RESERVADA);
        mesa.setNotasReserva(notasReserva);

        log.info("Mesa '{}' reservada exitosamente", mesa.getNumero());
        return mesaMapper.toResponse(mesaRepository.save(mesa));
    }

    @Override
    @Transactional
    public MesaResponseDTO cambiarEstadoRapido(Long id, CambioEstadoRequestDTO request) {
        log.info("Solicitud de cambio de estado rápido para mesa ID: {} -> {}", id, request.getNuevoEstado());

        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));

        EstadoMesa nuevo = request.getNuevoEstado();

        if (nuevo == EstadoMesa.OCUPADA) {
            log.error("Transición inválida: Se intentó cambiar de estado a OCUPADA sin el flujo de asignación");
            throw new BusinessException("No puedes forzar el estado OCUPADA desde aquí. Utiliza el flujo de apertura.");
        }

        if (nuevo == EstadoMesa.LIBRE) {
            log.debug("Limpiando metadatos y asignaciones previas de la mesa '{}'", mesa.getNumero());
            mesa.setPedidoId(null);
            mesa.setMeseroId(null);
            mesa.setNotasReserva(null);
        }

        mesa.setEstado(nuevo);
        log.info("Estado de la mesa '{}' actualizado a {}", mesa.getNumero(), nuevo);
        return mesaMapper.toResponse(mesaRepository.save(mesa));
    }
}
