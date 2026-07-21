package com.Sistema.Backend;

import com.Sistema.Backend.Categorias.Dto.MenuCategoriaDTO;
import com.Sistema.Backend.Categorias.Entity.Categoria;
import com.Sistema.Backend.Categorias.Repository.CategoriaRepository;
import com.Sistema.Backend.Exception.BadRequestException;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Productos.Dto.Request.ProductoRequestDTO;
import com.Sistema.Backend.Productos.Dto.Response.ProductoResponseDTO;
import com.Sistema.Backend.Productos.Entity.Producto;
import com.Sistema.Backend.Productos.Mapper.ProductoMapper;
import com.Sistema.Backend.Productos.Repository.ProductoRepository;
import com.Sistema.Backend.Productos.Services.Impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Categoria categoria;
    private Producto producto;
    private ProductoRequestDTO productoRequestDTO;
    private ProductoResponseDTO productoResponseDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Bebidas");
        categoria.setOrden(1);
        categoria.setActivo(true);
        categoria.setProductos(new ArrayList<>());

        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Coca Cola 600ml");
        producto.setDescripcion("Refresco embotellado");
        producto.setPrecio(new BigDecimal("25.00"));
        producto.setDisponible(true);
        producto.setActivo(true);
        producto.setCategoria(categoria);

        productoRequestDTO = new ProductoRequestDTO();
        productoRequestDTO.setNombre("Coca Cola 600ml");
        productoRequestDTO.setDescripcion("Refresco embotellado");
        productoRequestDTO.setPrecio(new BigDecimal("25.00"));
        productoRequestDTO.setDisponible(true);
        productoRequestDTO.setCategoriaId(1L);

        productoResponseDTO = new ProductoResponseDTO();
        productoResponseDTO.setId(10L);
        productoResponseDTO.setNombre("Coca Cola 600ml");
        productoResponseDTO.setNombreCategoria("Bebidas");
        productoResponseDTO.setPrecio(new BigDecimal("25.00"));
    }

    // =========================================================================
    // 1. PRUEBAS PARA: crear()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para crear producto")
    class CrearProductoTests {

        @Test
        @DisplayName("Debe crear producto asociando la categoría correctamente")
        void crear_ExitoConCategoria() {
            // Given
            given(productoMapper.toEntity(productoRequestDTO)).willReturn(producto);
            given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoria));
            given(productoRepository.save(producto)).willReturn(producto);
            given(productoMapper.toResponseDTO(producto)).willReturn(productoResponseDTO);

            // When
            ProductoResponseDTO resultado = productoService.crear(productoRequestDTO);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(productoResponseDTO);
            assertThat(producto.getCategoria()).isEqualTo(categoria);

            then(categoriaRepository).should(times(1)).findById(1L);
            then(productoRepository).should(times(1)).save(producto);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si la categoría indicada no existe")
        void crear_CategoriaNoExiste_LanzaExcepcion() {
            // Given
            given(productoMapper.toEntity(productoRequestDTO)).willReturn(producto);
            given(categoriaRepository.findById(1L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> productoService.crear(productoRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("La categoría asociada con ID 1 no existe");

            then(productoRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 2. PRUEBAS PARA: actualizar()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para actualizar producto")
    class ActualizarProductoTests {

        @Test
        @DisplayName("Debe actualizar los campos del producto exitosamente")
        void actualizar_Exito() {
            // Given
            given(productoRepository.findById(10L)).willReturn(Optional.of(producto));
            given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoria));
            given(productoRepository.save(producto)).willReturn(producto);
            given(productoMapper.toResponseDTO(producto)).willReturn(productoResponseDTO);

            // When
            ProductoResponseDTO resultado = productoService.actualizar(10L, productoRequestDTO);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(productoResponseDTO);
            then(productoRepository).should(times(1)).save(producto);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el producto a actualizar no existe")
        void actualizar_ProductoNoExiste_LanzaExcepcion() {
            // Given
            given(productoRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> productoService.actualizar(99L, productoRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Producto con ID 99 no encontrado");

            then(productoRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 3. PRUEBAS PARA: eliminar() (Soft Delete)
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para eliminar (Soft Delete)")
    class EliminarProductoTests {

        @Test
        @DisplayName("Debe aplicar baja lógica desactivando y marcando como no disponible el producto")
        void eliminar_Exito() {
            // Given
            given(productoRepository.findById(10L)).willReturn(Optional.of(producto));

            // When
            productoService.eliminar(10L);

            // Then
            assertThat(producto.isDisponible()).isFalse();
            assertThat(producto.getActivo()).isFalse();
            then(productoRepository).should(times(1)).save(producto);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el producto a eliminar no existe")
        void eliminar_NoExiste_LanzaExcepcion() {
            // Given
            given(productoRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> productoService.eliminar(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No se puede eliminar: Producto no encontrado");

            then(productoRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 4. PRUEBAS PARA: cambiarDisponibilidad()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para cambiarDisponibilidad")
    class CambiarDisponibilidadTests {

        @Test
        @DisplayName("Debe cambiar el estado de disponibilidad comercial del producto")
        void cambiarDisponibilidad_Exito() {
            // Given
            given(productoRepository.findById(10L)).willReturn(Optional.of(producto));

            // When
            productoService.cambiarDisponibilidad(10L, false);

            // Then
            assertThat(producto.isDisponible()).isFalse();
            then(productoRepository).should(times(1)).save(producto);
        }
    }

    // =========================================================================
    // 5. PRUEBAS PARA: actualizarPreciosMasivo()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para actualización masiva de precios")
    class ActualizarPreciosMasivoTests {

        @Test
        @DisplayName("Debe incrementar los precios en un 10% correctamente")
        void actualizarPreciosMasivo_IncrementoExito() {
            // Given
            given(productoRepository.findAll()).willReturn(List.of(producto)); // Precio inicial: 25.00

            // When
            productoService.actualizarPreciosMasivo(10.0); // +10%

            // Then
            assertThat(producto.getPrecio()).isEqualTo(new BigDecimal("27.50"));
            then(productoRepository).should(times(1)).saveAll(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el porcentaje es menor a -100%")
        void actualizarPreciosMasivo_PorcentajeInvalido_LanzaExcepcion() {
            // When / Then
            assertThatThrownBy(() -> productoService.actualizarPreciosMasivo(-105.0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El porcentaje de actualización masiva no puede ser menor a -100%");

            then(productoRepository).should(never()).saveAll(any());
        }
    }

    // =========================================================================
    // 6. PRUEBAS PARA: Consultas y Agrupaciones (Menú Digital)
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para consultas y estructuración del menú")
    class ConsultasMenuTests {

        @Test
        @DisplayName("Debe agrupar productos disponibles por nombre de categoría")
        void listarMenuPorCategoria_Exito() {
            // Given
            given(productoRepository.findByDisponibleTrue()).willReturn(List.of(producto));
            given(productoMapper.toResponseDTO(producto)).willReturn(productoResponseDTO);

            // When
            Map<String, List<ProductoResponseDTO>> menu = productoService.listarMenuPorCategoria();

            // Then
            assertThat(menu).isNotNull().containsKey("Bebidas");
            assertThat(menu.get("Bebidas")).hasSize(1).contains(productoResponseDTO);
        }

        @Test
        @DisplayName("Debe construir el menú digital omitiendo categorías sin productos activos")
        void obtenerMenuDigital_Exito() {
            // Given
            categoria.getProductos().add(producto);
            given(categoriaRepository.findByActivoTrueOrderByOrdenAsc()).willReturn(List.of(categoria));
            given(productoMapper.toResponseDTO(producto)).willReturn(productoResponseDTO);

            // When
            List<MenuCategoriaDTO> menuDigital = productoService.obtenerMenuDigital();

            // Then
            assertThat(menuDigital).hasSize(1);
            assertThat(menuDigital.get(0).getNombre()).isEqualTo("Bebidas");
            assertThat(menuDigital.get(0).getProductos()).hasSize(1).contains(productoResponseDTO);
        }

        @Test
        @DisplayName("Debe filtrar productos de forma paginada")
        void listarPaginado_Exito() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Producto> pageProducto = new PageImpl<>(List.of(producto));

            // Opcion recomendada: Usar eq() o any() para evitar que espacios extra en el parametro rompan el stubbing
            given(productoRepository.buscarConFiltrosPaginados(any(), eq(1L), eq(true), eq(pageable)))
                    .willReturn(pageProducto);
            given(productoMapper.toResponseDTO(producto)).willReturn(productoResponseDTO);

            // When
            Page<ProductoResponseDTO> resultado = productoService.listarPaginado("Coca ", 1L, true, pageable);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1).contains(productoResponseDTO);

            then(productoRepository).should(times(1))
                    .buscarConFiltrosPaginados(any(), eq(1L), eq(true), eq(pageable));
        }
    }
}
