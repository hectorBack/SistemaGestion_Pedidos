package com.Sistema.Backend;

import com.Sistema.Backend.Categorias.Dto.Request.CategoriaRequestDTO;
import com.Sistema.Backend.Categorias.Dto.Response.CategoriaResponseDTO;
import com.Sistema.Backend.Categorias.Entity.Categoria;
import com.Sistema.Backend.Categorias.Mapper.CategoriaMapper;
import com.Sistema.Backend.Categorias.Repository.CategoriaRepository;
import com.Sistema.Backend.Categorias.Services.Impl.CategoriaServiceImpl;
import com.Sistema.Backend.Exception.BadRequestException;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Productos.Entity.Producto;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria categoria;
    private CategoriaRequestDTO requestDTO;
    private CategoriaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Bebidas");
        categoria.setActivo(true);
        categoria.setOrden(0);
        categoria.setProductos(new ArrayList<>());

        requestDTO = new CategoriaRequestDTO();
        requestDTO.setNombre("Bebidas");
        requestDTO.setActivo(true);

        responseDTO = new CategoriaResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNombre("Bebidas");
        responseDTO.setActivo(true);
    }

    // =========================================================================
    // 1. PRUEBAS PARA: listarTodas()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para listarTodas")
    class ListarTodasTests {

        @Test
        @DisplayName("Debe retornar la lista de todas las categorías activas ordenadas")
        void listarTodas_Exito() {
            // Given
            given(categoriaRepository.findByActivoTrueOrderByOrdenAsc()).willReturn(List.of(categoria));
            given(categoriaMapper.toResponseDTO(categoria)).willReturn(responseDTO);

            // When
            List<CategoriaResponseDTO> resultado = categoriaService.listarTodas();

            // Then
            assertThat(resultado)
                    .isNotNull()
                    .hasSize(1)
                    .containsExactly(responseDTO);

            then(categoriaRepository).should(times(1)).findByActivoTrueOrderByOrdenAsc();
            then(categoriaMapper).should(times(1)).toResponseDTO(categoria);
        }
    }

    // =========================================================================
    // 2. PRUEBAS PARA: listarPaginado()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para listarPaginado")
    class ListarPaginadoTests {

        @Test
        @DisplayName("Debe retornar una página de categorías filtradas correctamente")
        void listarPaginado_Exito() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Categoria> pageCategorias = new PageImpl<>(List.of(categoria));

            given(categoriaRepository.buscarTodasParaAdminPaginado("Bebidas", true, pageable))
                    .willReturn(pageCategorias);
            given(categoriaMapper.toResponseDTO(categoria)).willReturn(responseDTO);

            // When
            Page<CategoriaResponseDTO> resultado = categoriaService.listarPaginado("Bebidas", true, pageable);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1).contains(responseDTO);
            assertThat(resultado.getTotalElements()).isEqualTo(1);

            then(categoriaRepository).should(times(1)).buscarTodasParaAdminPaginado("Bebidas", true, pageable);
        }

        @Test
        @DisplayName("Debe pasar null al repositorio si el parámetro nombre está vacío o es de espacios")
        void listarPaginado_NombreVacio_PasaNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Categoria> pageCategorias = new PageImpl<>(List.of(categoria));

            given(categoriaRepository.buscarTodasParaAdminPaginado(null, true, pageable))
                    .willReturn(pageCategorias);
            given(categoriaMapper.toResponseDTO(categoria)).willReturn(responseDTO);

            // When
            Page<CategoriaResponseDTO> resultado = categoriaService.listarPaginado("   ", true, pageable);

            // Then
            assertThat(resultado).isNotNull();
            then(categoriaRepository).should(times(1)).buscarTodasParaAdminPaginado(null, true, pageable);
        }
    }

    // =========================================================================
    // 3. PRUEBAS PARA: obtenerPorId()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Debe retornar DTO cuando la categoría existe")
        void obtenerPorId_Exito() {
            // Given
            given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoria));
            given(categoriaMapper.toResponseDTO(categoria)).willReturn(responseDTO);

            // When
            CategoriaResponseDTO resultado = categoriaService.obtenerPorId(1L);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(responseDTO);
            then(categoriaRepository).should(times(1)).findById(1L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando la categoría no existe")
        void obtenerPorId_NoExiste_LanzaExcepcion() {
            // Given
            given(categoriaRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> categoriaService.obtenerPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Categoría no encontrada con ID: 99");

            then(categoriaRepository).should(times(1)).findById(99L);
            then(categoriaMapper).should(never()).toResponseDTO(any());
        }
    }

    // =========================================================================
    // 4. PRUEBAS PARA: crear()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para crear")
    class CrearTests {

        @Test
        @DisplayName("Debe crear una nueva categoría exitosamente si no existe previa")
        void crear_NuevaCategoria_Exito() {
            // Given
            given(categoriaRepository.encontrarTodasPorNombreNativo(requestDTO.getNombre()))
                    .willReturn(Optional.empty());
            given(categoriaMapper.toEntity(requestDTO)).willReturn(categoria);
            given(categoriaRepository.save(categoria)).willReturn(categoria);
            given(categoriaMapper.toResponseDTO(categoria)).willReturn(responseDTO);

            // When
            CategoriaResponseDTO resultado = categoriaService.crear(requestDTO);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(responseDTO);
            assertThat(categoria.getActivo()).isTrue();

            then(categoriaRepository).should(times(1)).save(categoria);
        }

        @Test
        @DisplayName("SOFT DELETE RECOVERY: Debe reactivar la categoría si existía inactiva")
        void crear_CategoriaInactiva_ReactivacionExito() {
            // Given
            categoria.setActivo(false); // Estaba eliminada
            given(categoriaRepository.encontrarTodasPorNombreNativo(requestDTO.getNombre()))
                    .willReturn(Optional.of(categoria));
            given(categoriaRepository.save(categoria)).willReturn(categoria);
            given(categoriaMapper.toResponseDTO(categoria)).willReturn(responseDTO);

            // When
            CategoriaResponseDTO resultado = categoriaService.crear(requestDTO);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(categoria.getActivo()).isTrue(); // Verificamos reactivación

            then(categoriaRepository).should(times(1)).save(categoria);
            then(categoriaMapper).should(never()).toEntity(any()); // No creó nueva entidad
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la categoría ya existe y está activa")
        void crear_CategoriaYaExisteYActiva_LanzaExcepcion() {
            // Given
            categoria.setActivo(true);
            given(categoriaRepository.encontrarTodasPorNombreNativo(requestDTO.getNombre()))
                    .willReturn(Optional.of(categoria));

            // When / Then
            assertThatThrownBy(() -> categoriaService.crear(requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("La categoría 'Bebidas' ya existe.");

            then(categoriaRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 5. PRUEBAS PARA: actualizar()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para actualizar")
    class ActualizarTests {

        @Test
        @DisplayName("Debe actualizar la categoría y reactivar productos si se fuerza la reactivación")
        void actualizar_ReactivacionConProductosEnCascada_Exito() {
            // Given
            categoria.setActivo(false);
            Producto producto = new Producto();
            producto.setId(10L);
            producto.setActivo(false);
            categoria.setProductos(List.of(producto));

            requestDTO.setActivo(true);
            requestDTO.setNombre("Bebidas Actualizadas");

            given(categoriaRepository.encontrarPorIdNativo(1L)).willReturn(Optional.of(categoria));
            given(categoriaRepository.existsByNombreIgnoreCase("Bebidas Actualizadas")).willReturn(false);
            given(categoriaRepository.save(categoria)).willReturn(categoria);
            given(categoriaMapper.toResponseDTO(categoria)).willReturn(responseDTO);

            // When
            CategoriaResponseDTO resultado = categoriaService.actualizar(1L, requestDTO);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(categoria.getActivo()).isTrue();
            assertThat(producto.getActivo()).isTrue(); // Reactivado en cascada

            then(categoriaMapper).should(times(1)).updateEntityFromDTO(requestDTO, categoria);
            then(categoriaRepository).should(times(1)).save(categoria);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el ID no existe")
        void actualizar_IdNoExiste_LanzaExcepcion() {
            // Given
            given(categoriaRepository.encontrarPorIdNativo(1L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> categoriaService.actualizar(1L, requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Categoría no encontrada");

            then(categoriaRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el nuevo nombre ya pertenece a otra categoría")
        void actualizar_NombreDuplicado_LanzaExcepcion() {
            // Given
            requestDTO.setNombre("Postres"); // Nombre cambiado
            given(categoriaRepository.encontrarPorIdNativo(1L)).willReturn(Optional.of(categoria));
            given(categoriaRepository.existsByNombreIgnoreCase("Postres")).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> categoriaService.actualizar(1L, requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Ya existe otra categoría con el nombre: Postres");

            then(categoriaRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe desactivar manualmente la categoría si el DTO lo indica en false")
        void actualizar_DesactivarManualmente_Exito() {
            // Given
            requestDTO.setActivo(false);
            given(categoriaRepository.encontrarPorIdNativo(1L)).willReturn(Optional.of(categoria));
            given(categoriaRepository.save(categoria)).willReturn(categoria);
            given(categoriaMapper.toResponseDTO(categoria)).willReturn(responseDTO);

            // When
            categoriaService.actualizar(1L, requestDTO);

            // Then
            assertThat(categoria.getActivo()).isFalse();
            then(categoriaRepository).should(times(1)).save(categoria);
        }
    }

    // =========================================================================
    // 6. PRUEBAS PARA: eliminar()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para eliminar (Soft Delete)")
    class EliminarTests {

        @Test
        @DisplayName("Debe aplicar soft delete a la categoría y sus productos asociados en cascada")
        void eliminar_SoftDeleteCascada_Exito() {
            // Given
            Producto producto = new Producto();
            producto.setId(10L);
            producto.setNombre("Coca Cola");
            producto.setActivo(true);
            categoria.setProductos(List.of(producto));

            given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoria));

            // When
            categoriaService.eliminar(1L);

            // Then
            assertThat(categoria.getActivo()).isFalse();
            assertThat(producto.getActivo()).isFalse(); // Apagado en cascada

            then(categoriaRepository).should(times(1)).save(categoria);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si la categoría a eliminar no existe")
        void eliminar_NoExiste_LanzaExcepcion() {
            // Given
            given(categoriaRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> categoriaService.eliminar(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Categoría no encontrada.");

            then(categoriaRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 7. PRUEBAS PARA: actualizarOrden()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para actualizarOrden")
    class ActualizarOrdenTests {

        @Test
        @DisplayName("Debe actualizar el orden de las categorías según el índice recibido")
        void actualizarOrden_Exito() {
            // Given
            List<Long> idsOrdenados = List.of(1L, 2L);
            Categoria cat2 = new Categoria();
            cat2.setId(2L);

            given(categoriaRepository.encontrarPorIdNativo(1L)).willReturn(Optional.of(categoria));
            given(categoriaRepository.encontrarPorIdNativo(2L)).willReturn(Optional.of(cat2));

            // When
            categoriaService.actualizarOrden(idsOrdenados);

            // Then
            assertThat(categoria.getOrden()).isEqualTo(0);
            assertThat(cat2.getOrden()).isEqualTo(1);

            then(categoriaRepository).should(times(1)).save(categoria);
            then(categoriaRepository).should(times(1)).save(cat2);
        }

        @Test
        @DisplayName("No debe ejecutar interacciones si la lista enviada es nula o vacía")
        void actualizarOrden_ListaVaciaONula_SinAcciones() {
            // When
            categoriaService.actualizarOrden(Collections.emptyList());
            categoriaService.actualizarOrden(null);

            // Then
            then(categoriaRepository).shouldHaveNoInteractions();
        }
    }
}
