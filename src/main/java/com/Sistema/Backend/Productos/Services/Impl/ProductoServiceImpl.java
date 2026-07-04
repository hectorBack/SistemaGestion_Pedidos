package com.Sistema.Backend.Productos.Services.Impl;

import com.Sistema.Backend.Categorias.Dto.MenuCategoriaDTO;
import com.Sistema.Backend.Categorias.Entity.Categoria;
import com.Sistema.Backend.Categorias.Repository.CategoriaRepository;
import com.Sistema.Backend.Productos.Dto.Request.ProductoRequestDTO;
import com.Sistema.Backend.Productos.Dto.Response.ProductoResponseDTO;
import com.Sistema.Backend.Productos.Entity.Producto;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Productos.Mapper.ProductoMapper;
import com.Sistema.Backend.Productos.Repository.ProductoRepository;
import com.Sistema.Backend.Productos.Services.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper;

    public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository, ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoMapper = productoMapper;
    }


    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO request) {
        log.info("Iniciando creación del producto: '{}'", request.getNombre());
        Producto producto = productoMapper.toEntity(request);
        ProductoResponseDTO resultado = productoMapper.toResponseDTO(productoRepository.save(producto));
        log.info("Producto creado exitosamente con ID: {}", resultado.getId());
        return resultado;
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO request) {
        log.info("Solicitud para actualizar producto ID: {}", id);
        // Usamos tu nueva excepción personalizada
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Fallo al actualizar: Producto ID {} no encontrado", id);
                    return new ResourceNotFoundException("Producto con ID " + id + " no encontrado");
                });

        actualizarCampos(producto, request);

        ProductoResponseDTO resultado = productoMapper.toResponseDTO(productoRepository.save(producto));
        log.info("Campos del producto ID {} actualizados correctamente", id);
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarTodos() {
        log.info("Solicitando listado completo de todos los productos de la base de datos");
        return productoRepository.findAll().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarDisponibles() {
        log.info("Filtrando catálogo comercial: Obteniendo productos disponibles para la venta");
        // MEJORA: Filtrar en DB, no en Java
        return productoRepository.findByDisponibleTrue().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        log.info("Solicitud de baja lógica e indisponibilidad para el producto ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Fallo al eliminar: Producto ID {} inexistente", id);
                    return new ResourceNotFoundException("No se puede eliminar: Producto no encontrado");
                });

        // Apagamos también la disponibilidad comercial por consistencia de datos
        producto.setDisponible(false);
        producto.setActivo(false);

        // Ejecuta el Soft Delete a través de la configuración de Hibernate
        productoRepository.save(producto);
        log.info("Soft delete aplicado exitosamente al producto ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        log.info("Buscando producto por ID: {}", id);
        return productoRepository.findById(id)
                .map(productoMapper::toResponseDTO)
                .orElseThrow(() -> {
                    log.error("Error: No se encontró el producto ID: {}", id);
                    return new EntityNotFoundException("Producto no encontrado");
                });
    }

    @Override
    @Transactional
    public void cambiarDisponibilidad(Long id, boolean disponible) {
        log.info("Modificación rápida de disponibilidad comercial para producto ID: {} -> disponible: {}", id, disponible);
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se pudo cambiar estado: Producto ID {} no encontrado", id);
                    return new EntityNotFoundException("Producto no encontrado");
                });

        producto.setDisponible(disponible);
        productoRepository.save(producto);
        log.info("Estado de disponibilidad del producto ID {} guardado con éxito", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<ProductoResponseDTO>> listarMenuPorCategoria() {
        // MEJORA: Solo traemos de la DB lo que necesitamos
        return productoRepository.findByDisponibleTrue().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.groupingBy(ProductoResponseDTO::getNombreCategoria));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        log.info("Generando menú estructurado por categorías para la visualización del cliente");
        // MEJORA: Búsqueda mediante query de base de datos
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void actualizarPreciosMasivo(double porcentaje) {
        log.warn("EJECUCIÓN DE PROCESO MASIVO: Actualizando precios de todo el catálogo en un {}%", porcentaje);
        // Ejemplo: si porcentaje es 10, multiplicamos por 1.10
        BigDecimal factor = BigDecimal.valueOf(1 + (porcentaje / 100));

        List<Producto> productos = productoRepository.findAll();
        productos.forEach(p -> {
            BigDecimal nuevoPrecio = p.getPrecio().multiply(factor);
            p.setPrecio(nuevoPrecio);
        });

        productoRepository.saveAll(productos);
        log.info("Precios masivos actualizados con éxito. Total de productos afectados: {}", productos.size());
    }

    @Override
    public Page<ProductoResponseDTO> listarPaginado(String nombre, Long categoriaId, Boolean disponible, Pageable pageable) {
        log.info("Búsqueda paginada avanzada de productos - Filtros -> Nombre: '{}', CategoriaID: {}, Disponible: {} | Pág: {}, Tamaño: {}",
                nombre, categoriaId, disponible, pageable.getPageNumber(), pageable.getPageSize());

        // Limpieza de filtros: Si vienen vacíos o con espacios, los pasamos como null
        String nombreFiltro = (nombre != null && !nombre.trim().isEmpty()) ? nombre : null;

        // Llamamos al método avanzado del Repository (usando el ID numérico de la categoría)
        Page<Producto> productosPaginados = productoRepository.buscarConFiltrosPaginados(nombreFiltro, categoriaId, disponible, pageable);

        // 🌟 ¡LA MAGIA! Convertimos la página de Entidades a una página de DTOs usando tu ProductoMapper
        return productosPaginados.map(productoMapper::toResponseDTO);
    }

    @Override
    public List<MenuCategoriaDTO> obtenerMenuDigital() {
        // 1. Buscamos todas las categorías activas ordenadas por su prioridad
        List<Categoria> categorias = categoriaRepository.findByActivoTrueOrderByOrdenAsc();
        // Asegúrate de tener este método en tu CategoriaRepository usando ORDER BY c.orden ASC

        List<MenuCategoriaDTO> menuCompleto = new ArrayList<>();

        for (Categoria cat : categorias) {
            MenuCategoriaDTO dto = new MenuCategoriaDTO();
            dto.setId(cat.getId());
            dto.setNombre(cat.getNombre());
            dto.setOrden(cat.getOrden());

            // Mapeamos únicamente sus productos activos
            List<ProductoResponseDTO> productosDTO = cat.getProductos().stream()
                    .filter(Producto::getActivo)
                    .map(productoMapper::toResponseDTO)
                    .collect(Collectors.toList());

            // Solo agregamos la categoría al menú si tiene productos (opcional)
            if (!productosDTO.isEmpty()) {
                dto.setProductos(productosDTO);
                menuCompleto.add(dto);
            }
        }
        return menuCompleto;
    }

    // Método privado para limpieza de código (Mantenibilidad)
    private void actualizarCampos(Producto producto, ProductoRequestDTO request) {
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setDisponible(request.isDisponible());

        if (request.getSabores() != null) {
            producto.setSabores(request.getSabores());
        }

        // MEJORA: Solo actualiza la URL si el DTO trae una nueva de Cloudinary
        if (request.getUrlImagen() != null && !request.getUrlImagen().trim().isEmpty()) {
            producto.setUrlImagen(request.getUrlImagen());
        }

        // Buscamos la entidad Categoria usando el categoriaId del DTO
        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> {
                        log.error("Fallo de integridad: Categoría ID {} no asociada al sistema", request.getCategoriaId());
                        return new RuntimeException("Categoría no encontrada con ID: " + request.getCategoriaId());
                    });
            producto.setCategoria(categoria);
        }
    }
}
