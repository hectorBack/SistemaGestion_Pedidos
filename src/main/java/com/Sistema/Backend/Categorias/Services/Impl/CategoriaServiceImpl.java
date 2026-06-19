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
        log.info("Solicitando listado completo de todas las categorías activas ordenadas por prioridad");

        // 🌟 CAMBIO AQUÍ: Cambiamos findAll() por tu nuevo método derivado ordenado
        return categoriaRepository.findByActivoTrueOrderByOrdenAsc().stream()
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
    @Transactional
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
        log.info("Solicitud para actualizar categoría ID: {} con nuevos datos de nombre: '{}'", id, dto.getNombre());

        Categoria categoria = categoriaRepository.encontrarPorIdNativo(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Validar unicidad de nombre si cambió...
        if (!categoria.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new RuntimeException("Ya existe otra categoría con el nombre: " + dto.getNombre());
        }

        // 🛠️ BLINDAJE: Si el DTO trae 'activo' en true, o si viene null pero el negocio dicta que al editar se puede reactivar
        // Forzamos la reactivación basándonos en lo que el DTO solicita explícitamente
        if (dto.getActivo() != null && dto.getActivo()) {
            if (Boolean.FALSE.equals(categoria.getActivo())) {
                log.info("🌟 FORZANDO REACTIVACIÓN: Cambiando estado de categoría ID: {} a TRUE", id);
                categoria.setActivo(true);

                if (categoria.getProductos() != null) {
                    categoria.getProductos().forEach(producto -> {
                        producto.setActivo(true);
                        log.info("-> Producto ID: {} reactivado en cascada", producto.getId());
                    });
                }
            }
        } else if (dto.getActivo() != null && !dto.getActivo()) {
            // Por si en el modal desmarcan el checkbox manualmente en lugar de usar el botón eliminar
            categoria.setActivo(false);
        }

        // Aplicar el resto de cambios del DTO (Nombre)
        categoriaMapper.updateEntityFromDTO(dto, categoria);

        log.info("Categoría ID: {} modificada con éxito", id);
        return categoriaMapper.toResponseDTO(categoriaRepository.save(categoria));
    }

    @Override
    public void eliminar(Long id) {
        log.info("Solicitud de baja lógica (Soft Delete) en cascada para la categoría ID: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Fallo al eliminar: Categoría ID: {} no existe", id);
                    return new RuntimeException("Categoría no encontrada.");
                });

        // 1. Cambiamos el estado de la categoría a falso
        categoria.setActivo(false);

        // 2. Apagamos en cascada todos los productos asociados para mantener la consistencia del menú
        if (categoria.getProductos() != null) {
            categoria.getProductos().forEach(producto -> {
                if (Boolean.TRUE.equals(producto.getActivo())) {
                    producto.setActivo(false);
                    log.info("-> Soft Delete aplicado en cascada al producto ID: {} ('{}')",
                            producto.getId(), producto.getNombre());
                }
            });
        }

        categoriaRepository.save(categoria);
        log.info("Soft Delete y cascada aplicados correctamente a la categoría ID: {}", id);
    }

    @Override
    @Transactional
    public void actualizarOrden(List<Long> idsOrdenados) {
        if (idsOrdenados == null || idsOrdenados.isEmpty()) {
            log.warn("Se recibió una lista de ordenamiento vacía o nula");
            return;
        }

        log.info("Actualizando el orden de {} categorías en ráfaga masiva", idsOrdenados.size());

        // Recorremos los IDs basándonos en su posición en el arreglo enviado por Vue
        for (int i = 0; i < idsOrdenados.size(); i++) {
            Long id = idsOrdenados.get(i);
            int nuevoOrden = i; // La posición en el índice (0, 1, 2...) será su prioridad

            // Buscamos usando el bypass nativo que ya tienes configurado
            categoriaRepository.encontrarPorIdNativo(id).ifPresent(categoria -> {
                categoria.setOrden(nuevoOrden);
                categoriaRepository.save(categoria);
                log.debug("-> Categoría ID: {} fijada en orden: {}", id, nuevoOrden);
            });
        }

        log.info("Sincronización de ordenamiento dinámico finalizada con éxito");
    }
}
