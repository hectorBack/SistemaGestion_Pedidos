package com.Sistema.Backend.Empleados.Mapper;

import com.Sistema.Backend.Empleados.Dto.Request.EmpleadoRequestDTO;
import com.Sistema.Backend.Empleados.Dto.Response.EmpleadoResponseDTO;
import com.Sistema.Backend.Empleados.Entity.Empleado;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmpleadoMapper {

    public Empleado toEntity(EmpleadoRequestDTO dto) {
        if (dto == null) return null;

        Empleado empleado = new Empleado();
        empleado.setNombre(dto.getNombre());
        empleado.setPuesto(dto.getPuesto());
        empleado.setActivo(true); // Por defecto se registra activo
        return empleado;
    }

    public EmpleadoResponseDTO toDto(Empleado entity) {
        if (entity == null) return null;

        EmpleadoResponseDTO dto = new EmpleadoResponseDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setPuesto(entity.getPuesto());
        dto.setActivo(entity.isActivo());
        return dto;
    }

    public List<EmpleadoResponseDTO> toDtoList(List<Empleado> empleados) {
        return empleados.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Auxiliar para cuando actualices un registro existente
    public void updateEntityFromDto(EmpleadoRequestDTO dto, Empleado entity) {
        if (dto == null || entity == null) return;
        entity.setNombre(dto.getNombre());
        entity.setPuesto(dto.getPuesto());
    }
}
