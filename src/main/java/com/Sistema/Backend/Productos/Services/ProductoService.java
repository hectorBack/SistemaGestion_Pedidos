package com.Sistema.Backend.Productos.Services;

import com.Sistema.Backend.Productos.Dto.Request.ProductoRequestDTO;
import com.Sistema.Backend.Productos.Dto.Response.ProductoResponseDTO;
import com.Sistema.Backend.Productos.Entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ProductoService {

    ProductoResponseDTO crear(ProductoRequestDTO request);
    ProductoResponseDTO actualizar(Long id, ProductoRequestDTO request);
    List<ProductoResponseDTO> listarTodos();
    List<ProductoResponseDTO> listarDisponibles(); // Para el link del cliente
    void eliminar(Long id);
    ProductoResponseDTO obtenerPorId(Long id);

    /**
     * Cambia la disponibilidad sin tener que enviar todo el objeto de nuevo.
     * Útil para cuando el mesero nota que se acabó un ingrediente.
     */
    void cambiarDisponibilidad(Long id, boolean disponible);

    /**
     * Para el link del cliente: agrupar por categorías (Entradas, Platos Fuertes, Bebidas).
     * Esto mejora mucho la experiencia de usuario (UX).
     */
    Map<String, List<ProductoResponseDTO>> listarMenuPorCategoria();

    /**
     * Búsqueda por nombre para el administrador.
     */
    List<ProductoResponseDTO> buscarPorNombre(String nombre);

    /**
     * Actualizar precio masivamente por porcentaje (Ej: subir 10% a todo por inflación).
     */
    void actualizarPreciosMasivo(double porcentaje);

    /**
     * Listar paginado
     */
    Page<ProductoResponseDTO> listarPaginado(String nombre, Long categoriaId, Boolean disponible, Pageable pageable);
}
