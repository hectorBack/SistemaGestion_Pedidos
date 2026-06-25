package com.Sistema.Backend.Empleados.Services;

import com.Sistema.Backend.Empleados.Dto.Request.EmpleadoRequestDTO;
import com.Sistema.Backend.Empleados.Dto.Response.EmpleadoResponseDTO;
import com.Sistema.Backend.Empleados.Entity.PuestoEmpleado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmpleadoService {

    EmpleadoResponseDTO crear(EmpleadoRequestDTO request);

    EmpleadoResponseDTO obtenerPorId(Long id);

    // Listar Paginado con filtros opcionales (Ideal para el Panel Admin)
    Page<EmpleadoResponseDTO> listarPaginado(String nombre, PuestoEmpleado puesto, Boolean activo, Pageable pageable);

    // Listar filtrado por puesto y activos (Sin paginar, ideal para el <select> del modal del Front)
    List<EmpleadoResponseDTO> listarActivosPorPuesto(PuestoEmpleado puesto);

    EmpleadoResponseDTO actualizar(Long id, EmpleadoRequestDTO request);

    void cambiarDisponibilidad(Long id, boolean activo);

    void eliminar(Long id);
}
