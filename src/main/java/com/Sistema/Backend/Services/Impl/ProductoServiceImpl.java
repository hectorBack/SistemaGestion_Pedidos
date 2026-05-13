package com.Sistema.Backend.Services.Impl;

import com.Sistema.Backend.Dto.Request.ProductoRequestDTO;
import com.Sistema.Backend.Dto.Response.ProductoResponseDTO;
import com.Sistema.Backend.Entity.Producto;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Mapper.ProductoMapper;
import com.Sistema.Backend.Repository.ProductoRepository;
import com.Sistema.Backend.Services.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    public ProductoServiceImpl(ProductoRepository productoRepository, ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
    }


    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO request) {
        Producto producto = productoMapper.toEntity(request);
        return productoMapper.toResponseDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO request) {
        // Usamos tu nueva excepción personalizada
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + id + " no encontrado"));

        actualizarCampos(producto, request);

        return productoMapper.toResponseDTO(productoRepository.save(producto));
    }

    @Override
    public List<ProductoResponseDTO> listarTodos() {
        return productoRepository.findAll().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponseDTO> listarDisponibles() {
        // MEJORA: Filtrar en DB, no en Java
        return productoRepository.findByDisponibleTrue().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar: Producto no encontrado");
        }
        productoRepository.deleteById(id);
    }

    @Override
    public ProductoResponseDTO obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .map(productoMapper::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
    }

    @Override
    @Transactional
    public void cambiarDisponibilidad(Long id, boolean disponible) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        producto.setDisponible(disponible);
        productoRepository.save(producto);
    }

    @Override
    public Map<String, List<ProductoResponseDTO>> listarMenuPorCategoria() {
        // MEJORA: Solo traemos de la DB lo que necesitamos
        return productoRepository.findByDisponibleTrue().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.groupingBy(ProductoResponseDTO::getCategoria));
    }

    @Override
    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        // MEJORA: Búsqueda mediante query de base de datos
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void actualizarPreciosMasivo(double porcentaje) {
        // Ejemplo: si porcentaje es 10, multiplicamos por 1.10
        BigDecimal factor = BigDecimal.valueOf(1 + (porcentaje / 100));

        List<Producto> productos = productoRepository.findAll();
        productos.forEach(p -> {
            BigDecimal nuevoPrecio = p.getPrecio().multiply(factor);
            p.setPrecio(nuevoPrecio);
        });

        productoRepository.saveAll(productos);
    }

    // Método privado para limpieza de código (Mantenibilidad)
    private void actualizarCampos(Producto producto, ProductoRequestDTO request) {
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setDisponible(request.isDisponible());
        producto.setUrlImagen(request.getUrlImagen());
        producto.setCategoria(request.getCategoria());
    }
}
