package com.Sistema.Backend.Empleados.Services.Impl;

import com.Sistema.Backend.Empleados.Dto.Request.EmpleadoRequestDTO;
import com.Sistema.Backend.Empleados.Dto.Response.EmpleadoResponseDTO;
import com.Sistema.Backend.Empleados.Entity.Empleado;
import com.Sistema.Backend.Empleados.Entity.PuestoEmpleado;
import com.Sistema.Backend.Empleados.Mapper.EmpleadoMapper;
import com.Sistema.Backend.Empleados.Repository.EmpleadoRepository;
import com.Sistema.Backend.Empleados.Services.EmpleadoService;
import com.Sistema.Backend.Exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final EmpleadoMapper empleadoMapper;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository, EmpleadoMapper empleadoMapper) {
        this.empleadoRepository = empleadoRepository;
        this.empleadoMapper = empleadoMapper;
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO crear(EmpleadoRequestDTO request) {
        log.info("Registrando nuevo empleado en el sistema: {} con puesto {}", request.getNombre(), request.getPuesto());
        Empleado empleado = empleadoMapper.toEntity(request);
        Empleado guardado = empleadoRepository.save(empleado);
        log.info("Empleado guardado con éxito. ID asignado: {}", guardado.getId());
        return empleadoMapper.toDto(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponseDTO obtenerPorId(Long id) {
        log.debug("Buscando empleado con ID: {}", id);
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se encontró el empleado con ID: {}", id);
                    return new BusinessException("No se encontró el empleado solicitado con ID: " + id);
                });
        return empleadoMapper.toDto(empleado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmpleadoResponseDTO> listarPaginado(String nombre, PuestoEmpleado puesto, Boolean activo, Pageable pageable) {
        log.info("Consultando lista paginada de empleados con filtros - Nombre: {}, Puesto: {}, Activo: {}", nombre, puesto, activo);

        // Creamos un objeto prototipo para filtros dinámicos con Query by Example (QBE)
        Empleado filtro = new Empleado();
        filtro.setNombre(nombre);
        filtro.setPuesto(puesto);
        if (activo != null) {
            filtro.setActivo(activo);
        } else {
            filtro.setActivo(true); // Filtro por defecto si viene nulo
        }

        // Configuramos el matcher para que busque coincidencias parciales ignorando mayúsculas en el nombre
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("nombre", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withIgnoreNullValues();

        Example<Empleado> example = Example.of(filtro, matcher);
        Page<Empleado> empleadosPage = empleadoRepository.findAll(example, pageable);

        log.debug("La consulta paginada retornó {} elementos", empleadosPage.getNumberOfElements());
        return empleadosPage.map(empleadoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> listarActivosPorPuesto(PuestoEmpleado puesto) {
        log.info("Recuperando empleados activos para el puesto: {}", puesto);
        List<Empleado> resultado = empleadoRepository.findByPuestoAndActivoTrue(puesto);
        return empleadoMapper.toDtoList(resultado);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO actualizar(Long id, EmpleadoRequestDTO request) {
        log.info("Actualizando datos del empleado con ID: {}", id);
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Fallo al actualizar. Empleado no encontrado con ID: {}", id);
                    return new BusinessException("No se puede actualizar. Empleado no encontrado.");
                });

        empleadoMapper.updateEntityFromDto(request, empleado);
        Empleado actualizado = empleadoRepository.save(empleado);
        log.info("Empleado ID: {} actualizado correctamente en DB", id);
        return empleadoMapper.toDto(actualizado);
    }

    @Override
    @Transactional
    public void cambiarDisponibilidad(Long id, boolean activo) {
        log.info("Cambiando estado de actividad del empleado ID: {} a -> {}", id, activo);
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Empleado no localizado."));

        empleado.setActivo(activo);
        empleadoRepository.save(empleado);
        log.info("Estado del empleado ID: {} actualizado con éxito", id);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        log.warn("Se ha solicitado la baja del empleado ID: {}", id);
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El empleado que intenta eliminar no existe."));

        // Aplicamos borrado lógico (Soft Delete) modificando el flag 'activo'
        empleado.setActivo(false);
        empleadoRepository.save(empleado);
        log.info("Baja lógica aplicada con éxito al empleado ID: {}", id);
    }
}
