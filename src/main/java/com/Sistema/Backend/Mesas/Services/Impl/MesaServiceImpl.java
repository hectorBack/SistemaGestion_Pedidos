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
import com.Sistema.Backend.Pedidos.Dto.Request.ComandaMesaRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Pedidos.Entity.EstadoPedido;
import com.Sistema.Backend.Pedidos.Services.PedidoService;
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
    private final PedidoService pedidoService;

    public MesaServiceImpl(MesaRepository mesaRepository, MesaMapper mesaMapper, PedidoService pedidoService) {
        this.mesaRepository = mesaRepository;
        this.mesaMapper = mesaMapper;
        this.pedidoService = pedidoService;
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
    public MesaResponseDTO abrirMesa(Long id, ComandaMesaRequestDTO comandaRequest, Long meseroId) {
        log.info("Iniciando apertura con ComandaMesaRequestDTO para mesa ID: {}", id);

        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada con ID: " + id));

        if (mesa.getEstado() == EstadoMesa.OCUPADA) {
            throw new BusinessException("La mesa ya se encuentra ocupada.");
        }

        if (meseroId == null) {
            throw new BusinessException("Es obligatorio asignar un mesero responsable.");
        }

        if (comandaRequest == null || comandaRequest.getItems() == null || comandaRequest.getItems().isEmpty()) {
            throw new BusinessException("No se puede abrir una mesa sin productos.");
        }

        // 🚀 Adaptamos el flujo creando el PedidoRequestDTO definitivo que exige el PedidoService
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO();
        pedidoRequest.setNombreCliente("Mesa " + mesa.getNumero());
        pedidoRequest.setItems(comandaRequest.getItems());
        pedidoRequest.setNotas(comandaRequest.getNotas());
        pedidoRequest.setPromocionId(comandaRequest.getPromocionId());

        // 🌟 Truco Clave: Ponemos un valor quemado de 10 dígitos para saltar el @Size(10) y el NOT NULL
        pedidoRequest.setWhatsappFinal("0000000000");

        // Delegamos la creación al servicio de pedidos
        PedidoResponseDTO pedidoGuardado = pedidoService.crearPedido(pedidoRequest);

        // Enlazamos la mesa
        mesa.setEstado(EstadoMesa.OCUPADA);
        mesa.setPedidoId(pedidoGuardado.getId());
        mesa.setMeseroId(meseroId);
        mesa.setNotasReserva(null);

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

        // 🚀 NUEVA LÓGICA DE NEGOCIO: El servicio ha terminado exitosamente
        if (nuevo == EstadoMesa.SUCIA) {
            if (mesa.getPedidoId() != null) {
                log.info("Finalizando servicio de la mesa '{}'. Cambiando Pedido ID: {} a ENTREGADO.", mesa.getNumero(), mesa.getPedidoId());

                // Cambiamos el estado del pedido de forma automática en la base de datos
                pedidoService.actualizarEstado(mesa.getPedidoId(), EstadoPedido.ENTREGADO);
            }
        }

        // 🧼 Limpieza de metadatos (Aplica cuando pasa a SUCIA para liberar el ID del pedido, o a LIBRE)
        if (nuevo == EstadoMesa.SUCIA || nuevo == EstadoMesa.LIBRE) {
            log.debug("Limpiando metadatos y enlaces de pedido para la mesa '{}'", mesa.getNumero());
            mesa.setPedidoId(null);
            mesa.setMeseroId(null);
            mesa.setNotasReserva(null);
        }

        mesa.setEstado(nuevo);
        log.info("Estado de la mesa '{}' actualizado con éxito a {}", mesa.getNumero(), nuevo);
        return mesaMapper.toResponse(mesaRepository.save(mesa));
    }
}
