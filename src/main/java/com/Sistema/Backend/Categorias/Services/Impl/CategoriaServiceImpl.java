package com.Sistema.Backend.Categorias.Services.Impl;

import com.Sistema.Backend.Categorias.Dto.Request.CategoriaRequestDTO;
import com.Sistema.Backend.Categorias.Dto.Response.CategoriaResponseDTO;
import com.Sistema.Backend.Categorias.Entity.Categoria;
import com.Sistema.Backend.Categorias.Mapper.CategoriaMapper;
import com.Sistema.Backend.Categorias.Repository.CategoriaRepository;
import com.Sistema.Backend.Categorias.Services.CategoriaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Override
    public List<CategoriaResponseDTO> listarTodas() {
        log.info("Solicitando listado completo de todas las categorías activas");
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> listarPaginado(String nombre, Boolean activo, Pageable pageable) {
        // 🌟 Log de SLF4J actualizado con el estado activo
        log.info("Búsqueda paginada de categorías - Filtro nombre: '{}' - Filtro Estado (Activo): {} - Página: {} - Tamaño: {}",
                nombre, activo, pageable.getPageNumber(), pageable.getPageSize());

        String filtroNombre = (nombre != null && !nombre.trim().isEmpty()) ? nombre : null;

        // Pasamos el filtro 'activo' directamente a la nueva query nativa
        Page<Categoria> categorias = categoriaRepository.buscarTodasParaAdminPaginado(filtroNombre, activo, pageable);

        return categorias.map(categoriaMapper::toResponseDTO);
    }

    @Override
    public CategoriaResponseDTO obtenerPorId(Long id) {
        log.info("Buscando categoría con ID: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
        return categoriaMapper.toResponseDTO(categoria);
    }

    @Override
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        log.info("Iniciando proceso de creación para la categoría: '{}'", dto.getNombre());
        Optional<Categoria> categoriaExistente = categoriaRepository.encontrarTodasPorNombreNativo(dto.getNombre());

        if (categoriaExistente.isPresent()) {
            Categoria cat = categoriaExistente.get();
            if (!cat.getActivo()) {
                log.warn("SOFT DELETE RECOVERY: La categoría '{}' ya existía pero estaba inactiva. Reactivándola.", dto.getNombre());
                cat.setActivo(true);
                return categoriaMapper.toResponseDTO(categoriaRepository.save(cat));
            } else {
                log.error("Fallo al crear: La categoría '{}' ya se encuentra activa en el sistema.", dto.getNombre());
                throw new RuntimeException("La categoría '" + dto.getNombre() + "' ya existe.");
            }
        }

        Categoria nuevaCategoria = categoriaMapper.toEntity(dto);
        nuevaCategoria.setActivo(true);
        CategoriaResponseDTO resultado = categoriaMapper.toResponseDTO(categoriaRepository.save(nuevaCategoria));
        log.info("Categoría '{}' creada exitosamente con ID: {}", resultado.getNombre(), resultado.getId());
        return resultado;
    }

    @Override
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
        log.info("Solicitud para actualizar categoría ID: {} con nuevos datos de nombre: '{}'", id, dto.getNombre());
        // Buscamos usando una query nativa o bypass para poder editar incluso si estuviera inactiva
        Categoria categoria = categoriaRepository.encontrarPorIdNativo(id)
                .orElseThrow(() -> {
                    log.error("Fallo al actualizar: No se encontró la categoría ID: {}", id);
                    return new RuntimeException("Categoría no encontrada");
                });

        if (!categoria.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            log.error("Conflict de nombres: Intentando renombrar a '{}', pero ese nombre ya pertenece a otra fila", dto.getNombre());
            throw new RuntimeException("Ya existe otra categoría con el nombre: " + dto.getNombre());
        }

        categoriaMapper.updateEntityFromDTO(dto, categoria);
        log.info("Categoría ID: {} modificada con éxito", id);
        return categoriaMapper.toResponseDTO(categoriaRepository.save(categoria));
    }

    @Override
    public void eliminar(Long id) {
        log.info("Solicitud de baja lógica (Soft Delete) para la categoría ID: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Fallo al eliminar: Categoría ID: {} no existe o ya cuenta con estado inactivo", id);
                    return new RuntimeException("Categoría no encontrada o ya está inactiva.");
                });

        // SOFT DELETE: Cambiamos el estado flag a falso en lugar de borrar la fila
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
        log.info("Soft Delete aplicado correctamente a la categoría ID: {}", id);
    }
}
