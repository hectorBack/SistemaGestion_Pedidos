package com.Sistema.Backend.Categorias.Services.Impl;

import com.Sistema.Backend.Categorias.Dto.Request.CategoriaRequestDTO;
import com.Sistema.Backend.Categorias.Dto.Response.CategoriaResponseDTO;
import com.Sistema.Backend.Categorias.Entity.Categoria;
import com.Sistema.Backend.Categorias.Mapper.CategoriaMapper;
import com.Sistema.Backend.Categorias.Repository.CategoriaRepository;
import com.Sistema.Backend.Categorias.Services.CategoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Override
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toResponseDTO) // 🌟 Usando el mapper
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoriaResponseDTO> listarPaginado(String nombre, Pageable pageable) {
        String filtroNombre = (nombre != null && !nombre.trim().isEmpty()) ? nombre : null;

        // 🌟 Llamamos a nuestra query especial de administración que ignora el filtro global
        Page<Categoria> categorias = categoriaRepository.buscarTodasParaAdminPaginado(filtroNombre, pageable);

        return categorias.map(categoriaMapper::toResponseDTO);
    }

    @Override
    public CategoriaResponseDTO obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
        return categoriaMapper.toResponseDTO(categoria);
    }

    @Override
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        Optional<Categoria> categoriaExistente = categoriaRepository.encontrarTodasPorNombreNativo(dto.getNombre());

        if (categoriaExistente.isPresent()) {
            Categoria cat = categoriaExistente.get();
            if (!cat.getActivo()) {
                // 🌟 SOFT DELETE RECOVERY: Si ya existía pero estaba inactiva, la reactivamos
                cat.setActivo(true);
                return categoriaMapper.toResponseDTO(categoriaRepository.save(cat));
            } else {
                throw new RuntimeException("La categoría '" + dto.getNombre() + "' ya existe.");
            }
        }

        Categoria nuevaCategoria = categoriaMapper.toEntity(dto);
        nuevaCategoria.setActivo(true);
        return categoriaMapper.toResponseDTO(categoriaRepository.save(nuevaCategoria));
    }

    @Override
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
        // Buscamos usando una query nativa o bypass para poder editar incluso si estuviera inactiva
        Categoria categoria = categoriaRepository.encontrarPorIdNativo(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (!categoria.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new RuntimeException("Ya existe otra categoría con el nombre: " + dto.getNombre());
        }

        categoriaMapper.updateEntityFromDTO(dto, categoria);
        return categoriaMapper.toResponseDTO(categoriaRepository.save(categoria));
    }

    @Override
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada o ya está inactiva."));

        // 🌟 SOFT DELETE: Cambiamos el estado flag a falso en lugar de borrar la fila
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }
}
