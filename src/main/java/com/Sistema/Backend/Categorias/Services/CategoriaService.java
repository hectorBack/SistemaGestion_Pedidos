package com.Sistema.Backend.Categorias.Services;

import com.Sistema.Backend.Categorias.Dto.Request.CategoriaRequestDTO;
import com.Sistema.Backend.Categorias.Dto.Response.CategoriaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoriaService {

    List<CategoriaResponseDTO> listarTodas();
    // Para la tabla de administración con paginación y filtro por nombre
    Page<CategoriaResponseDTO> listarPaginado(String nombre, Boolean activo, Pageable pageable);
    CategoriaResponseDTO obtenerPorId(Long id);
    CategoriaResponseDTO crear(CategoriaRequestDTO dto);
    CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto);
    void eliminar(Long id);
}
