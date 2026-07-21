package com.Sistema.Backend;

import com.Sistema.Backend.Exception.BadRequestException;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Productos.Entity.Producto;
import com.Sistema.Backend.Productos.Repository.ProductoRepository;
import com.Sistema.Backend.Promociones.Dto.PromocionStatsDTO;
import com.Sistema.Backend.Promociones.Dto.Request.PromocionRequestDTO;
import com.Sistema.Backend.Promociones.Dto.Response.PromocionResponseDTO;
import com.Sistema.Backend.Promociones.Entity.Promocion;
import com.Sistema.Backend.Promociones.Mapper.PromocionMapper;
import com.Sistema.Backend.Promociones.Repository.PromocionRepository;
import com.Sistema.Backend.Promociones.Services.Impl.PromocionServiceImpl;
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
import java.time.LocalDateTime;
import java.util.List;
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
public class PromocionServiceImplTest {

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PromocionMapper promocionMapper;

    @InjectMocks
    private PromocionServiceImpl promocionService;

    private Producto producto;
    private Promocion promocion;
    private PromocionRequestDTO promocionRequestDTO;
    private PromocionResponseDTO promocionResponseDTO;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Hamburguesa Doble");
        producto.setPrecio(new BigDecimal("150.00"));

        promocion = new Promocion();
        promocion.setId(1L);
        promocion.setNombre("Descuento de Verano");
        promocion.setDescripcion("20% de descuento en hamburguesas");
        promocion.setTipoDescuento("PORCENTAJE");
        promocion.setValor(new BigDecimal("20.00"));
        promocion.setFechaInicio(LocalDateTime.now().minusDays(1));
        promocion.setFechaFin(LocalDateTime.now().plusDays(5));
        promocion.setActiva(true);
        promocion.setProducto(producto);

        promocionRequestDTO = new PromocionRequestDTO();
        promocionRequestDTO.setNombre("Descuento de Verano");
        promocionRequestDTO.setDescripcion("20% de descuento en hamburguesas");
        promocionRequestDTO.setTipoDescuento("PORCENTAJE");
        promocionRequestDTO.setValor(new BigDecimal("20.00"));
        promocionRequestDTO.setFechaInicio(LocalDateTime.now().minusDays(1));
        promocionRequestDTO.setFechaFin(LocalDateTime.now().plusDays(5));
        promocionRequestDTO.setActiva(true);
        promocionRequestDTO.setProductoId(10L);

        promocionResponseDTO = new PromocionResponseDTO();
        promocionResponseDTO.setId(1L);
        promocionResponseDTO.setNombre("Descuento de Verano");
        promocionResponseDTO.setActiva(true);
    }

    // =========================================================================
    // 1. PRUEBAS PARA: crearPromocion()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para creación de promociones")
    class CrearPromocionTests {

        @Test
        @DisplayName("Debe crear una promoción ligada a un producto existente")
        void crearPromocion_ConProducto_Exito() {
            // Given
            given(promocionMapper.toEntity(promocionRequestDTO)).willReturn(promocion);
            given(productoRepository.findById(10L)).willReturn(Optional.of(producto));
            given(promocionRepository.save(promocion)).willReturn(promocion);
            given(promocionMapper.toResponseDTO(promocion)).willReturn(promocionResponseDTO);

            // When
            PromocionResponseDTO resultado = promocionService.crearPromocion(promocionRequestDTO);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(promocionResponseDTO);
            assertThat(promocion.getProducto()).isEqualTo(producto);

            then(productoRepository).should(times(1)).findById(10L);
            then(promocionRepository).should(times(1)).save(promocion);
        }

        @Test
        @DisplayName("Debe crear una promoción global si productoId es nulo")
        void crearPromocion_SinProducto_Exito() {
            // Given
            promocionRequestDTO.setProductoId(null);
            promocion.setProducto(null);

            given(promocionMapper.toEntity(promocionRequestDTO)).willReturn(promocion);
            given(promocionRepository.save(promocion)).willReturn(promocion);
            given(promocionMapper.toResponseDTO(promocion)).willReturn(promocionResponseDTO);

            // When
            PromocionResponseDTO resultado = promocionService.crearPromocion(promocionRequestDTO);

            // Then
            assertThat(resultado).isNotNull();
            then(productoRepository).should(never()).findById(any());
            then(promocionRepository).should(times(1)).save(promocion);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el producto asociado no existe")
        void crearPromocion_ProductoNoExiste_LanzaExcepcion() {
            // Given
            given(promocionMapper.toEntity(promocionRequestDTO)).willReturn(promocion);
            given(productoRepository.findById(10L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> promocionService.crearPromocion(promocionRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("El producto asociado con ID 10 no existe");

            then(promocionRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 2. PRUEBAS PARA: actualizarPromocion()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para actualización de promociones")
    class ActualizarPromocionTests {

        @Test
        @DisplayName("Debe actualizar correctamente la promoción y reasignar producto")
        void actualizarPromocion_Exito() {
            // Given
            given(promocionRepository.findById(1L)).willReturn(Optional.of(promocion));
            given(productoRepository.findById(10L)).willReturn(Optional.of(producto));
            given(promocionRepository.save(promocion)).willReturn(promocion);
            given(promocionMapper.toResponseDTO(promocion)).willReturn(promocionResponseDTO);

            // When
            PromocionResponseDTO resultado = promocionService.actualizarPromocion(1L, promocionRequestDTO);

            // Then
            assertThat(resultado).isNotNull();
            then(promocionRepository).should(times(1)).save(promocion);
        }

        @Test
        @DisplayName("Debe remover el producto asociado convirtiéndola en Global si el DTO no trae productoId")
        void actualizarPromocion_A_Global_Exito() {
            // Given
            promocionRequestDTO.setProductoId(null);
            given(promocionRepository.findById(1L)).willReturn(Optional.of(promocion));
            given(promocionRepository.save(promocion)).willReturn(promocion);
            given(promocionMapper.toResponseDTO(promocion)).willReturn(promocionResponseDTO);

            // When
            promocionService.actualizarPromocion(1L, promocionRequestDTO);

            // Then
            assertThat(promocion.getProducto()).isNull();
            then(productoRepository).should(never()).findById(any());
            then(promocionRepository).should(times(1)).save(promocion);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si la promoción a actualizar no existe")
        void actualizarPromocion_NoExiste_LanzaExcepcion() {
            // Given
            given(promocionRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> promocionService.actualizarPromocion(99L, promocionRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Promoción con ID 99 no encontrada");

            then(promocionRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 3. PRUEBAS PARA: desactivarPromocion() y activarPromocion()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas de cambio de estado (Activar / Desactivar)")
    class CambiarEstadoPromocionTests {

        @Test
        @DisplayName("Debe desactivar la promoción cambiando 'activa' a false")
        void desactivarPromocion_Exito() {
            // Given
            given(promocionRepository.findById(1L)).willReturn(Optional.of(promocion));

            // When
            promocionService.desactivarPromocion(1L);

            // Then
            assertThat(promocion.isActiva()).isFalse();
            then(promocionRepository).should(times(1)).save(promocion);
        }

        @Test
        @DisplayName("Debe activar exitosamente una promoción cuya fecha fin no ha expirado")
        void activarPromocion_Exito() {
            // Given
            promocion.setActiva(false);
            promocion.setFechaFin(LocalDateTime.now().plusDays(2)); // Aún vigente
            given(promocionRepository.findById(1L)).willReturn(Optional.of(promocion));

            // When
            promocionService.activarPromocion(1L);

            // Then
            assertThat(promocion.isActiva()).isTrue();
            then(promocionRepository).should(times(1)).save(promocion);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException al intentar activar una promoción expirada")
        void activarPromocion_Expirada_LanzaExcepcion() {
            // Given
            promocion.setFechaFin(LocalDateTime.now().minusDays(1)); // Expirada
            given(promocionRepository.findById(1L)).willReturn(Optional.of(promocion));

            // When / Then
            assertThatThrownBy(() -> promocionService.activarPromocion(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("No se puede activar una promoción que ya ha expirado");

            then(promocionRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 4. PRUEBAS PARA: Consultas y Listados
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para listados, estadísticas y paginación")
    class ConsultasPromocionTests {

        @Test
        @DisplayName("Debe listar promociones vigentes invocando el repositorio con la fecha actual")
        void listarPromocionesVigentes_Exito() {
            // Given
            given(promocionRepository.findPromocionesVigentes(any(LocalDateTime.class)))
                    .willReturn(List.of(promocion));
            given(promocionMapper.toResponseDTO(promocion)).willReturn(promocionResponseDTO);

            // When
            List<PromocionResponseDTO> resultado = promocionService.listarPromocionesVigentes();

            // Then
            assertThat(resultado).hasSize(1).contains(promocionResponseDTO);
            then(promocionRepository).should(times(1)).findPromocionesVigentes(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Debe realizar búsqueda paginada utilizando matchers flexibles")
        void listarPaginado_Exito() {
            // Given
            Pageable pageable = PageRequest.of(0, 5);
            Page<Promocion> pagePromocion = new PageImpl<>(List.of(promocion));

            // Uso de eq() y any() para prevenir inconsistencias con el trim() del service
            given(promocionRepository.buscarConFiltrosPaginados(any(), eq(true), eq(pageable)))
                    .willReturn(pagePromocion);
            given(promocionMapper.toResponseDTO(promocion)).willReturn(promocionResponseDTO);

            // When
            Page<PromocionResponseDTO> resultado = promocionService.listarPaginado(" Verano ", true, pageable);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1).contains(promocionResponseDTO);
            then(promocionRepository).should(times(1)).buscarConFiltrosPaginados(any(), eq(true), eq(pageable));
        }

        @Test
        @DisplayName("Debe calcular y retornar las estadísticas globales del módulo")
        void obtenerEstadisticasGlobales_Exito() {
            // Given
            given(promocionRepository.countTotal()).willReturn(10L);
            given(promocionRepository.countActivas(any(LocalDateTime.class))).willReturn(5L);
            given(promocionRepository.countProgramadas(any(LocalDateTime.class))).willReturn(3L);
            given(promocionRepository.countExpiradas(any(LocalDateTime.class))).willReturn(2L);

            // When
            PromocionStatsDTO stats = promocionService.obtenerEstadisticasGlobales();

            // Then
            assertThat(stats).isNotNull();
            assertThat(stats.getTotal()).isEqualTo(10L);
            assertThat(stats.getActivas()).isEqualTo(5L);
            assertThat(stats.getProgramadas()).isEqualTo(3L);
            assertThat(stats.getExpiradas()).isEqualTo(2L);
        }
    }
}
