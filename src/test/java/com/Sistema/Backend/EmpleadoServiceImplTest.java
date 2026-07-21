package com.Sistema.Backend;

import com.Sistema.Backend.Empleados.Dto.Request.EmpleadoRequestDTO;
import com.Sistema.Backend.Empleados.Dto.Response.EmpleadoResponseDTO;
import com.Sistema.Backend.Empleados.Entity.Empleado;
import com.Sistema.Backend.Empleados.Entity.PuestoEmpleado;
import com.Sistema.Backend.Empleados.Mapper.EmpleadoMapper;
import com.Sistema.Backend.Empleados.Repository.EmpleadoRepository;
import com.Sistema.Backend.Empleados.Services.Impl.EmpleadoServiceImpl;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class EmpleadoServiceImplTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private EmpleadoMapper empleadoMapper;

    @InjectMocks
    private EmpleadoServiceImpl empleadoService;

    private Empleado empleado;
    private EmpleadoRequestDTO requestDTO;
    private EmpleadoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        empleado = new Empleado();
        empleado.setId(1L);
        empleado.setNombre("Carlos Mendoza");
        empleado.setPuesto(PuestoEmpleado.MESERO);
        empleado.setActivo(true);

        requestDTO = new EmpleadoRequestDTO();
        requestDTO.setNombre("Carlos Mendoza");
        requestDTO.setPuesto(PuestoEmpleado.MESERO);

        responseDTO = new EmpleadoResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNombre("Carlos Mendoza");
        responseDTO.setPuesto(PuestoEmpleado.MESERO);
        responseDTO.setActivo(true);
    }

    // =========================================================================
    // 1. PRUEBAS PARA: crear()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para crear")
    class CrearTests {

        @Test
        @DisplayName("Debe crear y mapear un empleado correctamente")
        void crear_Exito() {
            // Given
            given(empleadoMapper.toEntity(requestDTO)).willReturn(empleado);
            given(empleadoRepository.save(empleado)).willReturn(empleado);
            given(empleadoMapper.toDto(empleado)).willReturn(responseDTO);

            // When
            EmpleadoResponseDTO resultado = empleadoService.crear(requestDTO);

            // Then
            assertThat(resultado)
                    .isNotNull()
                    .isEqualTo(responseDTO);

            then(empleadoMapper).should(times(1)).toEntity(requestDTO);
            then(empleadoRepository).should(times(1)).save(empleado);
            then(empleadoMapper).should(times(1)).toDto(empleado);
        }
    }

    // =========================================================================
    // 2. PRUEBAS PARA: obtenerPorId()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Debe retornar el DTO del empleado cuando existe el ID")
        void obtenerPorId_Exito() {
            // Given
            given(empleadoRepository.findById(1L)).willReturn(Optional.of(empleado));
            given(empleadoMapper.toDto(empleado)).willReturn(responseDTO);

            // When
            EmpleadoResponseDTO resultado = empleadoService.obtenerPorId(1L);

            // Then
            assertThat(resultado)
                    .isNotNull()
                    .isEqualTo(responseDTO);

            then(empleadoRepository).should(times(1)).findById(1L);
            then(empleadoMapper).should(times(1)).toDto(empleado);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el ID no existe")
        void obtenerPorId_NoExiste_LanzaExcepcion() {
            // Given
            given(empleadoRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> empleadoService.obtenerPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No se encontró el empleado solicitado con ID: 99");

            then(empleadoRepository).should(times(1)).findById(99L);
            then(empleadoMapper).should(never()).toDto(any());
        }
    }

    // =========================================================================
    // 3. PRUEBAS PARA: listarPaginado()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para listarPaginado")
    class ListarPaginadoTests {

        @Test
        @DisplayName("Debe buscar paginado aplicando los filtros explícitos enviados")
        void listarPaginado_ConFiltrosExplicitos_Exito() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Empleado> pageEmpleado = new PageImpl<>(List.of(empleado));

            given(empleadoRepository.findAll(any(Example.class), any(Pageable.class))).willReturn(pageEmpleado);
            given(empleadoMapper.toDto(empleado)).willReturn(responseDTO);

            // When
            Page<EmpleadoResponseDTO> resultado = empleadoService.listarPaginado("Carlos", PuestoEmpleado.MESERO, false, pageable);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1).contains(responseDTO);

            then(empleadoRepository).should(times(1)).findAll(any(Example.class), any(Pageable.class));
            then(empleadoMapper).should(times(1)).toDto(empleado);
        }

        @Test
        @DisplayName("Debe asignar 'activo = true' por defecto si el parámetro activo viene nulo")
        void listarPaginado_ActivoNull_AplicaTruePorDefecto() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Empleado> pageEmpleado = new PageImpl<>(List.of(empleado));

            given(empleadoRepository.findAll(any(Example.class), any(Pageable.class))).willReturn(pageEmpleado);
            given(empleadoMapper.toDto(empleado)).willReturn(responseDTO);

            // When
            Page<EmpleadoResponseDTO> resultado = empleadoService.listarPaginado("Carlos", PuestoEmpleado.MESERO, null, pageable);

            // Then
            assertThat(resultado).isNotNull();
            then(empleadoRepository).should(times(1)).findAll(any(Example.class), any(Pageable.class));
        }
    }

    // =========================================================================
    // 4. PRUEBAS PARA: listarActivosPorPuesto()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para listarActivosPorPuesto")
    class ListarActivosPorPuestoTests {

        @Test
        @DisplayName("Debe retornar la lista de empleados activos por su puesto")
        void listarActivosPorPuesto_Exito() {
            // Given
            List<Empleado> empleadosList = List.of(empleado);
            List<EmpleadoResponseDTO> dtoList = List.of(responseDTO);

            given(empleadoRepository.findByPuestoAndActivoTrue(PuestoEmpleado.MESERO)).willReturn(empleadosList);
            given(empleadoMapper.toDtoList(empleadosList)).willReturn(dtoList);

            // When
            List<EmpleadoResponseDTO> resultado = empleadoService.listarActivosPorPuesto(PuestoEmpleado.MESERO);

            // Then
            assertThat(resultado)
                    .isNotNull()
                    .hasSize(1)
                    .containsExactly(responseDTO);

            then(empleadoRepository).should(times(1)).findByPuestoAndActivoTrue(PuestoEmpleado.MESERO);
            then(empleadoMapper).should(times(1)).toDtoList(empleadosList);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay empleados para el puesto indicado")
        void listarActivosPorPuesto_SinResultados_RetornaListaVacia() {
            // Given
            given(empleadoRepository.findByPuestoAndActivoTrue(PuestoEmpleado.COCINERO)).willReturn(Collections.emptyList());
            given(empleadoMapper.toDtoList(Collections.emptyList())).willReturn(Collections.emptyList());

            // When
            List<EmpleadoResponseDTO> resultado = empleadoService.listarActivosPorPuesto(PuestoEmpleado.COCINERO);

            // Then
            assertThat(resultado).isNotNull().isEmpty();
            then(empleadoRepository).should(times(1)).findByPuestoAndActivoTrue(PuestoEmpleado.COCINERO);
        }
    }

    // =========================================================================
    // 5. PRUEBAS PARA: actualizar()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para actualizar")
    class ActualizarTests {

        @Test
        @DisplayName("Debe actualizar la entidad y retornar el DTO mapeado")
        void actualizar_Exito() {
            // Given
            requestDTO.setNombre("Carlos Mendoza Editado");
            given(empleadoRepository.findById(1L)).willReturn(Optional.of(empleado));
            given(empleadoRepository.save(empleado)).willReturn(empleado);
            given(empleadoMapper.toDto(empleado)).willReturn(responseDTO);

            // When
            EmpleadoResponseDTO resultado = empleadoService.actualizar(1L, requestDTO);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(responseDTO);

            then(empleadoRepository).should(times(1)).findById(1L);
            then(empleadoMapper).should(times(1)).updateEntityFromDto(requestDTO, empleado);
            then(empleadoRepository).should(times(1)).save(empleado);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el empleado a actualizar no existe")
        void actualizar_NoExiste_LanzaExcepcion() {
            // Given
            given(empleadoRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> empleadoService.actualizar(99L, requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No se puede actualizar. Empleado no encontrado.");

            then(empleadoRepository).should(times(1)).findById(99L);
            then(empleadoRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 6. PRUEBAS PARA: cambiarDisponibilidad()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para cambiarDisponibilidad")
    class CambiarDisponibilidadTests {

        @Test
        @DisplayName("Debe actualizar el flag activo al valor solicitado")
        void cambiarDisponibilidad_Exito() {
            // Given
            given(empleadoRepository.findById(1L)).willReturn(Optional.of(empleado));

            // When
            empleadoService.cambiarDisponibilidad(1L, false);

            // Then
            assertThat(empleado.isActivo()).isFalse();

            then(empleadoRepository).should(times(1)).findById(1L);
            then(empleadoRepository).should(times(1)).save(empleado);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si no se localiza al empleado")
        void cambiarDisponibilidad_NoExiste_LanzaExcepcion() {
            // Given
            given(empleadoRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> empleadoService.cambiarDisponibilidad(99L, true))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Empleado no localizado.");

            then(empleadoRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 7. PRUEBAS PARA: eliminar()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para eliminar (Soft Delete)")
    class EliminarTests {

        @Test
        @DisplayName("Debe realizar la baja lógica (Soft Delete) cambiando activo a false")
        void eliminar_SoftDelete_Exito() {
            // Given
            given(empleadoRepository.findById(1L)).willReturn(Optional.of(empleado));

            // When
            empleadoService.eliminar(1L);

            // Then
            assertThat(empleado.isActivo()).isFalse();

            then(empleadoRepository).should(times(1)).findById(1L);
            then(empleadoRepository).should(times(1)).save(empleado);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el empleado a eliminar no existe")
        void eliminar_NoExiste_LanzaExcepcion() {
            // Given
            given(empleadoRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> empleadoService.eliminar(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("El empleado que intenta eliminar no existe.");

            then(empleadoRepository).should(never()).save(any());
        }
    }
}
