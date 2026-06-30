package com.Sistema.Backend.Proveedores.Services.Impl;

import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Proveedores.Dto.Request.ProveedorRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.ProveedorResponseDTO;
import com.Sistema.Backend.Proveedores.Entity.Proveedor;
import com.Sistema.Backend.Proveedores.Mapper.ProveedorMapper;
import com.Sistema.Backend.Proveedores.Repository.ProveedorRepository;
import com.Sistema.Backend.Proveedores.Services.ProveedorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;

    public ProveedorServiceImpl(ProveedorRepository proveedorRepository, ProveedorMapper proveedorMapper) {
        this.proveedorRepository = proveedorRepository;
        this.proveedorMapper = proveedorMapper;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorResponseDTO> listarPaginado(String nombre, Boolean activo, Pageable pageable) {
        log.info("Buscando proveedores paginados - Filtros: nombre={}, activo={}", nombre, activo);
        return proveedorRepository.listarPaginado(nombre, activo, pageable)
                .map(proveedorMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> listarTodosActivos() {
        log.info("Obteniendo lista plana de todos los proveedores activos");
        return proveedorRepository.findByActivoTrue().stream()
                .map(proveedorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponseDTO obtenerPorId(Long id) {
        log.info("Buscando proveedor por ID: {}", id);
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se encontró el proveedor con ID: {}", id);
                    return new ResourceNotFoundException("Proveedor no encontrado con ID: " + id);
                });
        return proveedorMapper.toResponseDTO(proveedor);
    }

    @Override
    @Transactional
    public ProveedorResponseDTO crear(ProveedorRequestDTO dto) {
        log.info("Registrando nuevo proveedor con nombre: {}", dto.getNombre());

        if (proveedorRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            log.warn("Intento de duplicación de nombre de proveedor: {}", dto.getNombre());
            throw new IllegalArgumentException("Ya existe un proveedor registrado con ese nombre");
        }

        Proveedor proveedor = proveedorMapper.toEntity(dto);
        Proveedor guardado = proveedorRepository.save(proveedor);
        log.info("Proveedor creado exitosamente con ID: {}", guardado.getId());
        return proveedorMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO dto) {
        log.info("Actualizando proveedor con ID: {}", id);

        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));

        if (proveedorRepository.existsByNombreIgnoreCaseAndIdNot(dto.getNombre(), id)) {
            log.warn("Intento de duplicar nombre al actualizar proveedor ID {}: {}", id, dto.getNombre());
            throw new IllegalArgumentException("El nombre del proveedor ya está en uso por otro registro");
        }

        proveedorMapper.updateEntityFromDTO(dto, proveedor);
        log.info("Cambios aplicados correctamente al proveedor ID: {}", id);
        return proveedorMapper.toResponseDTO(proveedor); // Al terminar @Transactional se sincroniza en BD
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        log.info("Ejecutando baja lógica (Soft Delete) del proveedor ID: {}", id);
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));

        proveedor.setActivo(false); // Soft Delete
        log.info("Proveedor ID: {} marcado como inactivo con éxito", id);
    }
}
