package com.Sistema.Backend.Proveedores.Services;

import com.Sistema.Backend.Proveedores.Dto.Request.InsumoRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.InsumoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InsumoService {

    // Crear un nuevo insumo
    InsumoResponseDTO crearInsumo(InsumoRequestDTO request);

    // Actualizar un insumo existente
    InsumoResponseDTO actualizarInsumo(Long id, InsumoRequestDTO request);

    // Obtener un insumo por ID
    InsumoResponseDTO obtenerPorId(Long id);

    // Buscar insumos paginados con filtros (Nombre y Estado) para la tabla principal
    Page<InsumoResponseDTO> obtenerInsumosPaginados(String nombre, Boolean activo, Pageable pageable);

    // Obtener todos los insumos activos sin paginar (Ideal para el select del Modal de asignación)
    List<InsumoResponseDTO> obtenerTodosLosActivos();

    // Cambiar el estado del insumo a inactivo (Soft Delete)
    void eliminarInsumo(Long id);
}
