package com.Sistema.Backend.Promociones.Services.Impl;

import com.Sistema.Backend.Promociones.Dto.Request.PromocionRequestDTO;
import com.Sistema.Backend.Promociones.Dto.Response.PromocionResponseDTO;
import com.Sistema.Backend.Productos.Entity.Producto;
import com.Sistema.Backend.Promociones.Entity.Promocion;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Promociones.Mapper.PromocionMapper;
import com.Sistema.Backend.Productos.Repository.ProductoRepository;
import com.Sistema.Backend.Promociones.Repository.PromocionRepository;
import com.Sistema.Backend.Promociones.Services.PromocionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PromocionServiceImpl implements PromocionService {

    private final PromocionRepository promocionRepository;
    private final ProductoRepository productoRepository;
    private final PromocionMapper promocionMapper;

    public PromocionServiceImpl(PromocionRepository promocionRepository, ProductoRepository productoRepository, PromocionMapper promocionMapper) {
        this.promocionRepository = promocionRepository;
        this.productoRepository = productoRepository;
        this.promocionMapper = promocionMapper;
    }

    @Override
    @Transactional
    public PromocionResponseDTO crearPromocion(PromocionRequestDTO dto) {
        log.info("Iniciando la creación de una nueva promoción: '{}'", dto.getNombre());
        Promocion promocion = promocionMapper.toEntity(dto);
        PromocionResponseDTO resultado = promocionMapper.toResponseDTO(promocionRepository.save(promocion));
        log.info("Promoción creada exitosamente con ID: {}", resultado.getId());
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> listarPromocionesVigentes() {

        // Captura el tiempo exacto del entorno donde corre tu API
        LocalDateTime ahora = LocalDateTime.now();
        log.info("Consultando promociones vigentes para la fecha/hora del servidor: {}", ahora);

        return promocionRepository.findPromocionesVigentes(ahora).stream()
                .map(promocionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> listarTodas() {
        log.info("Solicitando listado histórico completo de todas las promociones");
        return promocionRepository.findAll().stream()
                .map(promocionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void desactivarPromocion(Long id) {
        log.info("Solicitud para dar de baja/desactivar la promoción ID: {}", id);
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Fallo al desactivar: No se encontró la promoción ID: {}", id);
                    return new ResourceNotFoundException("Promoción no encontrada con ID: " + id);
                });

        promocion.setActiva(false);
        promocionRepository.save(promocion);
        log.info("Promoción ID: {} desactivada correctamente", id);
    }

    @Override
    @Transactional
    public PromocionResponseDTO actualizarPromocion(Long id, PromocionRequestDTO request) {
        log.info("Solicitud para actualizar datos de la promoción ID: {}", id);
        // 1. Buscar el registro actual o lanzar la excepción personalizada
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Fallo al actualizar: Promoción ID {} inexistente", id);
                    return new ResourceNotFoundException("Promoción con ID " + id + " no encontrada");
                });

        actualizarCampos(promocion, request);
        PromocionResponseDTO resultado = promocionMapper.toResponseDTO(promocionRepository.save(promocion));
        log.info("Estructura de la promoción ID {} actualizada con éxito", id);
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromocionResponseDTO> listarPaginado(String nombre, Boolean activa, Pageable pageable) {
        log.info("Búsqueda paginada de promociones - Filtros -> Nombre: '{}', Activa: {} | Pág: {}, Tamaño: {}",
                nombre, activa, pageable.getPageNumber(), pageable.getPageSize());

        // 🌟 Limpieza del filtro de texto
        String nombreFiltro = (nombre != null && !nombre.trim().isEmpty()) ? nombre : null;

        // 🌟 Ejecutamos la consulta avanzada con filtros opcionales
        Page<Promocion> promocionesPaginadas = promocionRepository.buscarConFiltrosPaginados(nombreFiltro, activa, pageable);

        // 🌟 Mapeamos la página de entidades a DTOs
        return promocionesPaginadas.map(promocionMapper::toResponseDTO);
    }

    // Método privado para limpieza de código (Mantenibilidad)
    private void actualizarCampos(Promocion promocion, PromocionRequestDTO request) {
        promocion.setNombre(request.getNombre());
        promocion.setDescripcion(request.getDescripcion());
        promocion.setTipoDescuento(request.getTipoDescuento());
        promocion.setValor(request.getValor());
        promocion.setFechaInicio(request.getFechaInicio());
        promocion.setFechaFin(request.getFechaFin());
        promocion.setActiva(request.isActiva());

        // 💡 IMPORTANTE: Si la promoción puede estar ligada a un producto,
        // asegúrate de resolver la entidad Producto usando su ID si viene en el request.
        if (request.getProductoId() != null) {
            Producto producto = productoRepository.findById(request.getProductoId())
                    .orElseThrow(() -> {
                        log.error("Fallo de integridad: No se puede asociar la promoción al producto ID {}, no existe", request.getProductoId());
                        return new ResourceNotFoundException("Producto con ID " + request.getProductoId() + " no encontrado");
                    });
            promocion.setProducto(producto);
        } else {
            log.info("La promoción ID {} se configuró como GLOBAL (Aplica a todo el menú)", promocion.getId());
            promocion.setProducto(null); // Si es null, aplica a todo el menú
        }
    }
}
