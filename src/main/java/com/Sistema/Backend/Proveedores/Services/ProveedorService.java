package com.Sistema.Backend.Proveedores.Services;

import com.Sistema.Backend.Proveedores.Dto.Request.ProveedorRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.ProveedorResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProveedorService {

    Page<ProveedorResponseDTO> listarPaginado(String nombre, Boolean activo, Pageable pageable);
    List<ProveedorResponseDTO> listarTodosActivos();
    ProveedorResponseDTO obtenerPorId(Long id);
    ProveedorResponseDTO crear(ProveedorRequestDTO dto);
    ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO dto);
    void eliminar(Long id); // Soft Delete
}
