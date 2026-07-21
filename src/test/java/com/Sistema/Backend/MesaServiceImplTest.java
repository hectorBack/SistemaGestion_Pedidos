package com.Sistema.Backend;

import com.Sistema.Backend.Exception.BadRequestException;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Mesas.Dto.Request.CambioEstadoRequestDTO;
import com.Sistema.Backend.Mesas.Dto.Request.MesaRequestDTO;
import com.Sistema.Backend.Mesas.Dto.Response.MesaResponseDTO;
import com.Sistema.Backend.Mesas.Entity.EstadoMesa;
import com.Sistema.Backend.Mesas.Entity.Mesa;
import com.Sistema.Backend.Mesas.Mapper.MesaMapper;
import com.Sistema.Backend.Mesas.Repository.MesaRepository;
import com.Sistema.Backend.Mesas.Services.Impl.MesaServiceImpl;
import com.Sistema.Backend.Pedidos.Dto.Request.ComandaMesaRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Request.ItemPedidoRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Request.PedidoRequestDTO;
import com.Sistema.Backend.Pedidos.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Pedidos.Entity.EstadoPedido;
import com.Sistema.Backend.Pedidos.Services.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
public class MesaServiceImplTest {

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private MesaMapper mesaMapper;

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private MesaServiceImpl mesaService;

    private Mesa mesa;
    private MesaRequestDTO mesaRequestDTO;
    private MesaResponseDTO mesaResponseDTO;

    @BeforeEach
    void setUp() {
        mesa = new Mesa();
        mesa.setId(1L);
        mesa.setNumero("M1");
        mesa.setCapacidad(4);
        mesa.setEstado(EstadoMesa.LIBRE);

        mesaRequestDTO = new MesaRequestDTO();
        mesaRequestDTO.setNumero("M1");
        mesaRequestDTO.setCapacidad(4);

        mesaResponseDTO = new MesaResponseDTO();
        mesaResponseDTO.setId(1L);
        mesaResponseDTO.setNumero("M1");
        mesaResponseDTO.setCapacidad(4);
        mesaResponseDTO.setEstado(EstadoMesa.LIBRE);
    }

    // =========================================================================
    // 1. PRUEBAS PARA: obtenerTodas()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerTodas")
    class ObtenerTodasTests {

        @Test
        @DisplayName("Debe retornar la lista completa de mesas mapeadas")
        void obtenerTodas_Exito() {
            // Given
            given(mesaRepository.findAll()).willReturn(List.of(mesa));
            given(mesaMapper.toResponse(mesa)).willReturn(mesaResponseDTO);

            // When
            List<MesaResponseDTO> resultado = mesaService.obtenerTodas();

            // Then
            assertThat(resultado).isNotNull().hasSize(1).contains(mesaResponseDTO);
            then(mesaRepository).should(times(1)).findAll();
            then(mesaMapper).should(times(1)).toResponse(mesa);
        }
    }

    // =========================================================================
    // 2. PRUEBAS PARA: obtenerPorEstado()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerPorEstado")
    class ObtenerPorEstadoTests {

        @Test
        @DisplayName("Debe filtrar y retornar las mesas según el estado enviado")
        void obtenerPorEstado_Exito() {
            // Given
            given(mesaRepository.findByEstado(EstadoMesa.LIBRE)).willReturn(List.of(mesa));
            given(mesaMapper.toResponse(mesa)).willReturn(mesaResponseDTO);

            // When
            List<MesaResponseDTO> resultado = mesaService.obtenerPorEstado(EstadoMesa.LIBRE);

            // Then
            assertThat(resultado).isNotNull().hasSize(1).contains(mesaResponseDTO);
            then(mesaRepository).should(times(1)).findByEstado(EstadoMesa.LIBRE);
        }
    }

    // =========================================================================
    // 3. PRUEBAS PARA: obtenerPorId()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Debe retornar el DTO cuando la mesa existe")
        void obtenerPorId_Exito() {
            // Given
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));
            given(mesaMapper.toResponse(mesa)).willReturn(mesaResponseDTO);

            // When
            MesaResponseDTO resultado = mesaService.obtenerPorId(1L);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(mesaResponseDTO);
            then(mesaRepository).should(times(1)).findById(1L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el ID no existe")
        void obtenerPorId_NoExiste_LanzaExcepcion() {
            // Given
            given(mesaRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> mesaService.obtenerPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Mesa no encontrada con ID: 99");
        }
    }

    // =========================================================================
    // 4. PRUEBAS PARA: crearMesa()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para crearMesa")
    class CrearMesaTests {

        @Test
        @DisplayName("Debe crear una mesa exitosamente si el número no existe")
        void crearMesa_Exito() {
            // Given
            given(mesaRepository.existsByNumero("M1")).willReturn(false);
            given(mesaMapper.toEntity(mesaRequestDTO)).willReturn(mesa);
            given(mesaRepository.save(mesa)).willReturn(mesa);
            given(mesaMapper.toResponse(mesa)).willReturn(mesaResponseDTO);

            // When
            MesaResponseDTO resultado = mesaService.crearMesa(mesaRequestDTO);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(mesaResponseDTO);
            then(mesaRepository).should(times(1)).existsByNumero("M1");
            then(mesaRepository).should(times(1)).save(mesa);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el número de mesa ya está registrado")
        void crearMesa_NumeroDuplicado_LanzaExcepcion() {
            // Given
            given(mesaRepository.existsByNumero("M1")).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> mesaService.crearMesa(mesaRequestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Ya existe una mesa registrada con el número: M1");

            then(mesaRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 5. PRUEBAS PARA: actualizarMesa()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para actualizarMesa")
    class ActualizarMesaTests {

        @Test
        @DisplayName("Debe actualizar las propiedades de la mesa exitosamente")
        void actualizarMesa_Exito() {
            // Given
            mesaRequestDTO.setNumero("M1_UP");
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));
            given(mesaRepository.existsByNumero("M1_UP")).willReturn(false);
            given(mesaRepository.save(mesa)).willReturn(mesa);
            given(mesaMapper.toResponse(mesa)).willReturn(mesaResponseDTO);

            // When
            MesaResponseDTO resultado = mesaService.actualizarMesa(1L, mesaRequestDTO);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(mesa.getNumero()).isEqualTo("M1_UP");
            then(mesaRepository).should(times(1)).save(mesa);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si la mesa a actualizar no existe")
        void actualizarMesa_NoExiste_LanzaExcepcion() {
            // Given
            given(mesaRepository.findById(1L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> mesaService.actualizarMesa(1L, mesaRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Mesa no encontrada");

            then(mesaRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el nuevo número pertenece a otra mesa")
        void actualizarMesa_NumeroExiste_LanzaExcepcion() {
            // Given
            mesaRequestDTO.setNumero("M2");
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));
            given(mesaRepository.existsByNumero("M2")).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> mesaService.actualizarMesa(1L, mesaRequestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El número de mesa ya está en uso");

            then(mesaRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 6. PRUEBAS PARA: abrirMesa()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para abrirMesa")
    class AbrirMesaTests {

        private ComandaMesaRequestDTO comandaRequest;
        private PedidoResponseDTO pedidoResponseDTO;

        @BeforeEach
        void setUpComanda() {
            comandaRequest = new ComandaMesaRequestDTO();
            comandaRequest.setItems(List.of(new ItemPedidoRequestDTO()));
            comandaRequest.setNotas("Sin cebolla");

            pedidoResponseDTO = new PedidoResponseDTO();
            pedidoResponseDTO.setId(50L);
        }

        @Test
        @DisplayName("Debe abrir la mesa creando un pedido y cambiando el estado a OCUPADA")
        void abrirMesa_Exito() {
            // Given
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));
            given(pedidoService.crearPedido(any(PedidoRequestDTO.class))).willReturn(pedidoResponseDTO);
            given(mesaRepository.save(mesa)).willReturn(mesa);
            given(mesaMapper.toResponse(mesa)).willReturn(mesaResponseDTO);

            // When
            MesaResponseDTO resultado = mesaService.abrirMesa(1L, comandaRequest, 10L);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(mesa.getEstado()).isEqualTo(EstadoMesa.OCUPADA);
            assertThat(mesa.getPedidoId()).isEqualTo(50L);
            assertThat(mesa.getMeseroId()).isEqualTo(10L);
            assertThat(mesa.getNotasReserva()).isNull();

            then(pedidoService).should(times(1)).crearPedido(any(PedidoRequestDTO.class));
            then(mesaRepository).should(times(1)).save(mesa);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la mesa ya está OCUPADA")
        void abrirMesa_Ocupada_LanzaExcepcion() {
            // Given
            mesa.setEstado(EstadoMesa.OCUPADA);
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));

            // When / Then
            assertThatThrownBy(() -> mesaService.abrirMesa(1L, comandaRequest, 10L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("La mesa ya se encuentra ocupada.");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el meseroId es nulo")
        void abrirMesa_SinMesero_LanzaExcepcion() {
            // Given
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));

            // When / Then
            assertThatThrownBy(() -> mesaService.abrirMesa(1L, comandaRequest, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Es obligatorio asignar un mesero responsable.");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la comanda no tiene items")
        void abrirMesa_SinItems_LanzaExcepcion() {
            // Given
            comandaRequest.setItems(Collections.emptyList());
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));

            // When / Then
            assertThatThrownBy(() -> mesaService.abrirMesa(1L, comandaRequest, 10L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("No se puede abrir una mesa sin productos.");
        }
    }

    // =========================================================================
    // 7. PRUEBAS PARA: reservarMesa()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para reservarMesa")
    class ReservarMesaTests {

        @Test
        @DisplayName("Debe reservar la mesa si su estado es LIBRE y las notas no están vacías")
        void reservarMesa_Exito() {
            // Given
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));
            given(mesaRepository.save(mesa)).willReturn(mesa);
            given(mesaMapper.toResponse(mesa)).willReturn(mesaResponseDTO);

            // When
            MesaResponseDTO resultado = mesaService.reservarMesa(1L, "Reserva Cumpleaños");

            // Then
            assertThat(resultado).isNotNull();
            assertThat(mesa.getEstado()).isEqualTo(EstadoMesa.RESERVADA);
            assertThat(mesa.getNotasReserva()).isEqualTo("Reserva Cumpleaños");

            then(mesaRepository).should(times(1)).save(mesa);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la mesa no está LIBRE")
        void reservarMesa_NoLibre_LanzaExcepcion() {
            // Given
            mesa.setEstado(EstadoMesa.OCUPADA);
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));

            // When / Then
            assertThatThrownBy(() -> mesaService.reservarMesa(1L, "Notas"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Solo se pueden reservar mesas que estén en estado LIBRE");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la nota de reserva está en blanco")
        void reservarMesa_NotasEnBlanco_LanzaExcepcion() {
            // Given
            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));

            // When / Then
            assertThatThrownBy(() -> mesaService.reservarMesa(1L, "   "))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Es necesario especificar las notas de la reservación");
        }
    }

    // =========================================================================
    // 8. PRUEBAS PARA: cambiarEstadoRapido()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para cambiarEstadoRapido")
    class CambiarEstadoRapidoTests {

        @Test
        @DisplayName("Debe cambiar estado a SUCIA y marcar pedido a ENTREGADO si tiene pedidoId vinculado")
        void cambiarEstadoRapido_ASucia_ConPedido_Exito() {
            // Given
            mesa.setPedidoId(50L);
            mesa.setMeseroId(10L);
            CambioEstadoRequestDTO request = new CambioEstadoRequestDTO();
            request.setNuevoEstado(EstadoMesa.SUCIA);

            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));
            given(mesaRepository.save(mesa)).willReturn(mesa);
            given(mesaMapper.toResponse(mesa)).willReturn(mesaResponseDTO);

            // When
            MesaResponseDTO resultado = mesaService.cambiarEstadoRapido(1L, request);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(mesa.getEstado()).isEqualTo(EstadoMesa.SUCIA);
            assertThat(mesa.getPedidoId()).isNull(); // Limpieza de metadatos
            assertThat(mesa.getMeseroId()).isNull();

            then(pedidoService).should(times(1)).actualizarEstado(50L, EstadoPedido.ENTREGADO);
            then(mesaRepository).should(times(1)).save(mesa);
        }

        @Test
        @DisplayName("Debe cambiar estado a LIBRE y limpiar metadatos de la mesa")
        void cambiarEstadoRapido_ALibre_Exito() {
            // Given
            mesa.setNotasReserva("Nota anterior");
            CambioEstadoRequestDTO request = new CambioEstadoRequestDTO();
            request.setNuevoEstado(EstadoMesa.LIBRE);

            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));
            given(mesaRepository.save(mesa)).willReturn(mesa);
            given(mesaMapper.toResponse(mesa)).willReturn(mesaResponseDTO);

            // When
            MesaResponseDTO resultado = mesaService.cambiarEstadoRapido(1L, request);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(mesa.getEstado()).isEqualTo(EstadoMesa.LIBRE);
            assertThat(mesa.getNotasReserva()).isNull();

            then(pedidoService).should(never()).actualizarEstado(any(), any());
            then(mesaRepository).should(times(1)).save(mesa);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si se intenta cambiar forzadamente a OCUPADA")
        void cambiarEstadoRapido_AOcupada_LanzaExcepcion() {
            // Given
            CambioEstadoRequestDTO request = new CambioEstadoRequestDTO();
            request.setNuevoEstado(EstadoMesa.OCUPADA);

            given(mesaRepository.findById(1L)).willReturn(Optional.of(mesa));

            // When / Then
            assertThatThrownBy(() -> mesaService.cambiarEstadoRapido(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("No puedes forzar el estado OCUPADA desde aquí. Utiliza el flujo de apertura.");

            then(mesaRepository).should(never()).save(any());
        }
    }
}
