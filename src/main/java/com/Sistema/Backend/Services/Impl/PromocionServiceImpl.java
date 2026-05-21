package com.Sistema.Backend.Services.Impl;

import com.Sistema.Backend.Dto.Request.PromocionRequestDTO;
import com.Sistema.Backend.Dto.Response.PromocionResponseDTO;
import com.Sistema.Backend.Entity.Producto;
import com.Sistema.Backend.Entity.Promocion;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Mapper.PromocionMapper;
import com.Sistema.Backend.Repository.ProductoRepository;
import com.Sistema.Backend.Repository.PromocionRepository;
import com.Sistema.Backend.Services.PromocionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
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
        // 1. Convertimos DTO a Entidad
        Promocion promocion = promocionMapper.toEntity(dto);

        // 2. Guardamos en PostgreSQL
        Promocion promocionGuardada = promocionRepository.save(promocion);

        // 3. Retornamos la respuesta mapeada
        return promocionMapper.toResponseDTO(promocionGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> listarPromocionesVigentes() {

        // Captura el tiempo exacto del entorno donde corre tu API
        LocalDateTime ahora = LocalDateTime.now();

        return promocionRepository.findPromocionesVigentes(ahora).stream()
                .map(promocionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromocionResponseDTO> listarTodas() {
        return promocionRepository.findAll().stream()
                .map(promocionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void desactivarPromocion(Long id) {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada con ID: " + id));
        promocion.setActiva(false);
        promocionRepository.save(promocion);
    }

    @Override
    @Transactional
    public PromocionResponseDTO actualizarPromocion(Long id, PromocionRequestDTO request) {
        // 1. Buscar el registro actual o lanzar la excepción personalizada
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción con ID " + id + " no encontrada"));

        // 2. Mapear/Actualizar los campos nuevos sobre la entidad gestionada por JPA
        actualizarCampos(promocion, request);

        // 3. Guardar cambios y retornar el DTO de respuesta mapeado
        return promocionMapper.toResponseDTO(promocionRepository.save(promocion));
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
                    .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + request.getProductoId() + " no encontrado"));
            promocion.setProducto(producto);
        } else {
            promocion.setProducto(null); // Si es null, aplica a todo el menú
        }
    }
}
