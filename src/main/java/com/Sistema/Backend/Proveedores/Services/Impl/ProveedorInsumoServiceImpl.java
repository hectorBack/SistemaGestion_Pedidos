package com.Sistema.Backend.Proveedores.Services.Impl;

import com.Sistema.Backend.Categorias.Entity.ProveedorInsumo;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Proveedores.Dto.Request.ProveedorInsumoRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.ProveedorInsumoResponseDTO;
import com.Sistema.Backend.Proveedores.Entity.Proveedor;
import com.Sistema.Backend.Proveedores.Mapper.ProveedorInsumoMapper;
import com.Sistema.Backend.Proveedores.Repository.ProveedorInsumoRepository;
import com.Sistema.Backend.Proveedores.Repository.ProveedorRepository;
import com.Sistema.Backend.Proveedores.Services.ProveedorInsumoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProveedorInsumoServiceImpl implements ProveedorInsumoService {

    private final ProveedorInsumoRepository proveedorInsumoRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProveedorInsumoMapper proveedorInsumoMapper;

    public ProveedorInsumoServiceImpl(ProveedorInsumoRepository proveedorInsumoRepository,
                                      ProveedorRepository proveedorRepository,
                                      ProveedorInsumoMapper proveedorInsumoMapper) {
        this.proveedorInsumoRepository = proveedorInsumoRepository;
        this.proveedorRepository = proveedorRepository;
        this.proveedorInsumoMapper = proveedorInsumoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorInsumoResponseDTO> listarPorProveedor(Long proveedorId, Boolean activo, Pageable pageable) {
        log.info("Consultando catálogo de insumos del proveedor ID: {} - activo={}", proveedorId, activo);
        if (!proveedorRepository.existsById(proveedorId)) {
            throw new ResourceNotFoundException("El proveedor con ID " + proveedorId + " no existe");
        }
        return proveedorInsumoRepository.findByProveedorIdAndActivo(proveedorId, activo, pageable)
                .map(proveedorInsumoMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorInsumoResponseDTO> obtenerProveedoresPorInsumo(Long insumoId) {
        log.info("Buscando qué proveedores surten el insumo ID: {}", insumoId);
        return proveedorInsumoRepository.findByInsumoIdAndActivoTrue(insumoId).stream()
                .map(proveedorInsumoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProveedorInsumoResponseDTO asignarOActualizarCosto(ProveedorInsumoRequestDTO dto) {
        log.info("Asignando/Actualizando costo. Proveedor ID: {}, Insumo ID: {}", dto.getProveedorId(), dto.getInsumoId());

        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + dto.getProveedorId()));

        // 🌟 Manejo inteligente de registros: Buscamos si ya existía la relación histórica (activa o inactiva)
        Optional<ProveedorInsumo> relacionExistente = proveedorInsumoRepository
                .findByProveedorIdAndInsumoId(dto.getProveedorId(), dto.getInsumoId());

        ProveedorInsumo entidadAProcesar;

        if (relacionExistente.isPresent()) {
            log.info("La relación ya existía en la base de datos. Actualizando valores y reactivando en caso de soft delete.");
            entidadAProcesar = relacionExistente.get();
            proveedorInsumoMapper.updateEntityFromDTO(dto, entidadAProcesar);
            entidadAProcesar.setActivo(true); // Se fuerza a true por si estaba borrado lógicamente
        } else {
            log.info("Creando un nuevo registro de costo e insumo para el proveedor.");
            entidadAProcesar = proveedorInsumoMapper.toEntity(dto);
            entidadAProcesar.setProveedor(proveedor);
        }

        ProveedorInsumo guardado = proveedorInsumoRepository.save(entidadAProcesar);
        return proveedorInsumoMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public void eliminarRelacion(Long id) {
        log.info("Removiendo de forma lógica el insumo del catálogo del proveedor con relación ID: {}", id);
        ProveedorInsumo registro = proveedorInsumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el registro de costo con ID: " + id));

        registro.setActivo(false); // Soft Delete del catálogo del proveedor
        log.info("Relación ID: {} dada de baja exitosamente", id);
    }
}
