package com.Sistema.Backend.Proveedores.Services.Impl;

import com.Sistema.Backend.Proveedores.Dto.Request.InsumoRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.InsumoResponseDTO;
import com.Sistema.Backend.Proveedores.Entity.Insumo;
import com.Sistema.Backend.Proveedores.Mapper.InsumoMapper;
import com.Sistema.Backend.Proveedores.Repository.InsumoRepository;
import com.Sistema.Backend.Proveedores.Services.InsumoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InsumoServiceImpl implements InsumoService {

    private final InsumoRepository insumoRepository;
    private final InsumoMapper insumoMapper;

    public InsumoServiceImpl(InsumoRepository insumoRepository, InsumoMapper insumoMapper) {
        this.insumoRepository = insumoRepository;
        this.insumoMapper = insumoMapper;
    }

    @Override
    @Transactional
    public InsumoResponseDTO crearInsumo(InsumoRequestDTO request) {
        log.info("Iniciando la creación de un nuevo insumo: {}", request.getNombre());

        Insumo nuevoInsumo = insumoMapper.toEntity(request);
        Insumo guardado = insumoRepository.save(nuevoInsumo);

        log.info("Insumo guardado exitosamente con ID: {}", guardado.getId());
        return insumoMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public InsumoResponseDTO actualizarInsumo(Long id, InsumoRequestDTO request) {
        log.info("Solicitud para actualizar el insumo con ID: {}", id);

        Insumo insumoExistente = insumoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se pudo actualizar. Insumo con ID {} no fue encontrado", id);
                    return new EntityNotFoundException("Insumo no encontrado con el ID: " + id);
                });

        insumoMapper.updateEntityFromDTO(request, insumoExistente);
        Insumo actualizado = insumoRepository.save(insumoExistente);

        log.info("Insumo con ID {} actualizado correctamente", actualizado.getId());
        return insumoMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public InsumoResponseDTO obtenerPorId(Long id) {
        log.info("Buscando insumo con ID: {}", id);

        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Insumo con ID {} no existe en el sistema", id);
                    return new EntityNotFoundException("Insumo no encontrado");
                });

        return insumoMapper.toResponseDTO(insumo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InsumoResponseDTO> obtenerInsumosPaginados(String nombre, Boolean activo, Pageable pageable) {
        log.info("Buscando lista paginada de insumos. Filtros -> Nombre: '{}', Activo: {}", nombre, activo);
        Page<Insumo> paginaInsumos;

        boolean tieneNombre = (nombre != null && !nombre.trim().isEmpty());
        boolean tieneEstado = (activo != null);

        if (tieneNombre && tieneEstado) {
            paginaInsumos = insumoRepository.findByNombreContainingIgnoreCaseAndActivo(nombre, activo, pageable);
        } else if (tieneNombre) {
            // 🌟 NUEVO: Busca por nombre sin importar si está activo o inactivo
            paginaInsumos = insumoRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else if (tieneEstado) {
            paginaInsumos = insumoRepository.findByActivo(activo, pageable);
        } else {
            // 🌟 NUEVO: Si no hay filtros (Todos los estados y nombre vacío), trae todo el catálogo
            paginaInsumos = insumoRepository.findAll(pageable);
        }

        log.info("Búsqueda finalizada. Total de elementos encontrados: {}", paginaInsumos.getTotalElements());
        return paginaInsumos.map(insumoMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> obtenerTodosLosActivos() {
        log.info("Cargando todos los insumos activos sin paginar para selectores en UI.");

        // Creamos un Pageable virtual no paginado o un truco rápido mapeando de la lista global limpia
        List<Insumo> activos = insumoRepository.findAll().stream()
                .filter(Insumo::getActivo)
                .collect(Collectors.toList());

        log.info("Se enviarán {} insumos activos a la vista del modal.", activos.size());
        return activos.stream()
                .map(insumoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarInsumo(Long id) {
        log.info("Solicitud de eliminación lógica (Soft Delete) para el insumo con ID: {}", id);

        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se pudo eliminar. Insumo con ID {} no fue encontrado", id);
                    return new EntityNotFoundException("Insumo no encontrado con el ID: " + id);
                });

        // 🌟 SOFT DELETE: En lugar de usar insumoRepository.delete(), cambiamos su estado
        insumo.setActivo(false);
        insumoRepository.save(insumo);

        log.info("Insumo con ID {} ha sido desactivado (eliminado lógicamente) con éxito", id);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, Boolean activo) {
        log.info("Solicitud para cambiar el estado del insumo con ID: {} a activo={}", id, activo);

        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se pudo cambiar el estado. Insumo con ID {} no fue encontrado", id);
                    return new EntityNotFoundException("Insumo no encontrado con el ID: " + id);
                });

        insumo.setActivo(activo);
        insumoRepository.save(insumo);

        log.info("Estado del insumo con ID {} actualizado exitosamente a: {}", id, activo);
    }
}
