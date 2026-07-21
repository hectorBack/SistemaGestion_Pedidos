package com.Sistema.Backend;

import com.Sistema.Backend.Clientes.Dto.Request.ClienteRequestDTO;
import com.Sistema.Backend.Clientes.Dto.Response.ClienteResponseDTO;
import com.Sistema.Backend.Clientes.Entity.Cliente;
import com.Sistema.Backend.Clientes.Mapper.ClienteMapper;
import com.Sistema.Backend.Clientes.Repository.ClienteRepository;
import com.Sistema.Backend.Clientes.Services.Impl.ClienteServiceImpl;
import com.Sistema.Backend.Exception.BadRequestException;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Usuarios.Entity.Rol;
import com.Sistema.Backend.Usuarios.Entity.TipoRol;
import com.Sistema.Backend.Usuarios.Entity.Usuario;
import com.Sistema.Backend.Usuarios.Repository.RolRepository;
import com.Sistema.Backend.Usuarios.Repository.UsuarioRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente cliente;
    private Usuario usuario;
    private Rol rolCliente;
    private ClienteRequestDTO requestDTO;
    private ClienteResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        rolCliente = new Rol();
        rolCliente.setId(1L);
        rolCliente.setNombre(TipoRol.CLIENTE);

        usuario = Usuario.builder()
                .id(10L)
                .username("johndoe")
                .email("john@example.com")
                .password("encoded_password")
                .roles(Set.of(rolCliente))
                .activo(true)
                .build();

        cliente = Cliente.builder()
                .id(1L)
                .nombreCompleto("John Doe")
                .telefono("123456789")
                .direccionEntrega("Calle Falsa 123")
                .usuario(usuario)
                .activo(true)
                .build();

        requestDTO = new ClienteRequestDTO();
        requestDTO.setUsername("johndoe");
        requestDTO.setEmail("john@example.com");
        requestDTO.setPassword("password123");
        requestDTO.setNombreCompleto("John Doe");
        requestDTO.setTelefono("123456789");
        requestDTO.setDireccionEntrega("Calle Falsa 123");

        responseDTO = ClienteResponseDTO.builder()
                .id(1L)
                .usuarioId(10L)
                .username("johndoe")
                .email("john@example.com")
                .nombreCompleto("John Doe")
                .telefono("123456789")
                .direccionEntrega("Calle Falsa 123")
                .activo(true)
                .roles(Set.of("CLIENTE"))
                .build();
    }

    // =========================================================================
    // 1. PRUEBAS PARA: registrarClientePublico()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para registrarClientePublico")
    class RegistrarClientePublicoTests {

        @Test
        @DisplayName("Debe registrar un nuevo cliente exitosamente")
        void registrarClientePublico_Exito() {
            // Given
            given(usuarioRepository.existsByUsername(requestDTO.getUsername())).willReturn(false);
            given(usuarioRepository.existsByEmail(requestDTO.getEmail())).willReturn(false);
            given(rolRepository.findByNombre(TipoRol.CLIENTE)).willReturn(Optional.of(rolCliente));
            given(passwordEncoder.encode(requestDTO.getPassword())).willReturn("encoded_password");
            given(clienteRepository.save(any(Cliente.class))).willReturn(cliente);
            given(clienteMapper.toResponseDTO(cliente)).willReturn(responseDTO);

            // When
            ClienteResponseDTO resultado = clienteService.registrarClientePublico(requestDTO);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(responseDTO);

            then(usuarioRepository).should(times(1)).existsByUsername(requestDTO.getUsername());
            then(usuarioRepository).should(times(1)).existsByEmail(requestDTO.getEmail());
            then(rolRepository).should(times(1)).findByNombre(TipoRol.CLIENTE);
            then(passwordEncoder).should(times(1)).encode("password123");
            then(clienteRepository).should(times(1)).save(any(Cliente.class));
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el username ya existe")
        void registrarClientePublico_UsernameExistente_LanzaExcepcion() {
            // Given
            given(usuarioRepository.existsByUsername(requestDTO.getUsername())).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> clienteService.registrarClientePublico(requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El nombre de usuario ya está en uso");

            then(usuarioRepository).should(never()).existsByEmail(any());
            then(clienteRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el email ya existe")
        void registrarClientePublico_EmailExistente_LanzaExcepcion() {
            // Given
            given(usuarioRepository.existsByUsername(requestDTO.getUsername())).willReturn(false);
            given(usuarioRepository.existsByEmail(requestDTO.getEmail())).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> clienteService.registrarClientePublico(requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El correo electrónico ya está registrado");

            then(rolRepository).should(never()).findByNombre(any());
            then(clienteRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el Rol CLIENTE no existe")
        void registrarClientePublico_RolNoExiste_LanzaExcepcion() {
            // Given
            given(usuarioRepository.existsByUsername(requestDTO.getUsername())).willReturn(false);
            given(usuarioRepository.existsByEmail(requestDTO.getEmail())).willReturn(false);
            given(rolRepository.findByNombre(TipoRol.CLIENTE)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> clienteService.registrarClientePublico(requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Error en la configuración del sistema. El rol CLIENTE no existe.");

            then(clienteRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 2. PRUEBAS PARA: listarClientesPaginados()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para listarClientesPaginados")
    class ListarClientesPaginadosTests {

        @Test
        @DisplayName("Debe retornar una página con clientes mapeados")
        void listarClientesPaginados_Exito() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Cliente> clientePage = new PageImpl<>(List.of(cliente));

            given(clienteRepository.buscarClientesPaginados("John", true, pageable)).willReturn(clientePage);
            given(clienteMapper.toResponseDTO(cliente)).willReturn(responseDTO);

            // When
            Page<ClienteResponseDTO> resultado = clienteService.listarClientesPaginados("John", true, pageable);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1).contains(responseDTO);

            then(clienteRepository).should(times(1)).buscarClientesPaginados("John", true, pageable);
            then(clienteMapper).should(times(1)).toResponseDTO(cliente);
        }
    }

    // =========================================================================
    // 3. PRUEBAS PARA: obtenerPorId()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Debe retornar DTO cuando el cliente existe")
        void obtenerPorId_Exito() {
            // Given
            given(clienteRepository.findById(1L)).willReturn(Optional.of(cliente));
            given(clienteMapper.toResponseDTO(cliente)).willReturn(responseDTO);

            // When
            ClienteResponseDTO resultado = clienteService.obtenerPorId(1L);

            // Then
            assertThat(resultado).isNotNull().isEqualTo(responseDTO);
            then(clienteRepository).should(times(1)).findById(1L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando el ID no existe")
        void obtenerPorId_NoExiste_LanzaExcepcion() {
            // Given
            given(clienteRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> clienteService.obtenerPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Cliente no encontrado con el ID especificado");

            then(clienteMapper).should(never()).toResponseDTO(any());
        }
    }

    // =========================================================================
    // 4. PRUEBAS PARA: actualizarCliente()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para actualizarCliente")
    class ActualizarClienteTests {

        @Test
        @DisplayName("Debe actualizar los datos del cliente incluyendo la contraseña si se proporciona")
        void actualizarCliente_ConPassword_Exito() {
            // Given
            requestDTO.setUsername("johndoe_updated");
            requestDTO.setEmail("john_updated@example.com");
            requestDTO.setPassword("newpass123");

            given(clienteRepository.findById(1L)).willReturn(Optional.of(cliente));
            given(usuarioRepository.existsByUsername("johndoe_updated")).willReturn(false);
            given(usuarioRepository.existsByEmail("john_updated@example.com")).willReturn(false);
            given(passwordEncoder.encode("newpass123")).willReturn("encoded_new_password");
            given(clienteRepository.save(cliente)).willReturn(cliente);
            given(clienteMapper.toResponseDTO(cliente)).willReturn(responseDTO);

            // When
            ClienteResponseDTO resultado = clienteService.actualizarCliente(1L, requestDTO);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(usuario.getUsername()).isEqualTo("johndoe_updated");
            assertThat(usuario.getEmail()).isEqualTo("john_updated@example.com");
            assertThat(usuario.getPassword()).isEqualTo("encoded_new_password");

            then(passwordEncoder).should(times(1)).encode("newpass123");
            then(clienteRepository).should(times(1)).save(cliente);
        }

        @Test
        @DisplayName("Debe actualizar sin encriptar contraseña si esta viene nula o en blanco")
        void actualizarCliente_SinPassword_Exito() {
            // Given
            requestDTO.setPassword("   "); // Blanco

            given(clienteRepository.findById(1L)).willReturn(Optional.of(cliente));
            given(clienteRepository.save(cliente)).willReturn(cliente);
            given(clienteMapper.toResponseDTO(cliente)).willReturn(responseDTO);

            // When
            clienteService.actualizarCliente(1L, requestDTO);

            // Then
            then(passwordEncoder).should(never()).encode(any());
            then(clienteRepository).should(times(1)).save(cliente);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el cliente no existe")
        void actualizarCliente_NoExiste_LanzaExcepcion() {
            // Given
            given(clienteRepository.findById(1L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> clienteService.actualizarCliente(1L, requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Cliente no encontrado");

            then(clienteRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el nuevo username ya lo usa otra cuenta")
        void actualizarCliente_UsernameDuplicado_LanzaExcepcion() {
            // Given
            requestDTO.setUsername("otroUsername");
            given(clienteRepository.findById(1L)).willReturn(Optional.of(cliente));
            given(usuarioRepository.existsByUsername("otroUsername")).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> clienteService.actualizarCliente(1L, requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El nombre de usuario ya está asignado a otra cuenta");

            then(clienteRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el nuevo email ya lo usa otra cuenta")
        void actualizarCliente_EmailDuplicado_LanzaExcepcion() {
            // Given
            requestDTO.setEmail("otro@email.com");
            given(clienteRepository.findById(1L)).willReturn(Optional.of(cliente));
            given(usuarioRepository.existsByEmail("otro@email.com")).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> clienteService.actualizarCliente(1L, requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El correo electrónico ya está asignado a otra cuenta");

            then(clienteRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 5. PRUEBAS PARA: cambiarEstadoActivo()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para cambiarEstadoActivo (Soft Delete)")
    class CambiarEstadoActivoTests {

        @Test
        @DisplayName("Debe cambiar el estado activo a false en cliente y usuario")
        void cambiarEstadoActivo_Desactivar_Exito() {
            // Given
            given(clienteRepository.findById(1L)).willReturn(Optional.of(cliente));

            // When
            clienteService.cambiarEstadoActivo(1L, false);

            // Then
            assertThat(cliente.isActivo()).isFalse();
            assertThat(cliente.getUsuario().isActivo()).isFalse();

            then(clienteRepository).should(times(1)).save(cliente);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el cliente no existe")
        void cambiarEstadoActivo_NoExiste_LanzaExcepcion() {
            // Given
            given(clienteRepository.findById(1L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> clienteService.cambiarEstadoActivo(1L, false))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Cliente no encontrado");

            then(clienteRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 6. PRUEBAS PARA: obtenerPerfilPorUsername()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerPerfilPorUsername")
    class ObtenerPerfilPorUsernameTests {

        @Test
        @DisplayName("Debe retornar el DTO del perfil construido manualmente desde el cliente")
        void obtenerPerfilPorUsername_Exito() {
            // Given
            given(clienteRepository.findByUsuario_Username("johndoe")).willReturn(Optional.of(cliente));

            // When
            ClienteResponseDTO resultado = clienteService.obtenerPerfilPorUsername("johndoe");

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getUsuarioId()).isEqualTo(10L);
            assertThat(resultado.getUsername()).isEqualTo("johndoe");
            assertThat(resultado.getRoles()).contains("CLIENTE");

            then(clienteRepository).should(times(1)).findByUsuario_Username("johndoe");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el perfil por username no existe")
        void obtenerPerfilPorUsername_NoExiste_LanzaExcepcion() {
            // Given
            given(clienteRepository.findByUsuario_Username("noexiste")).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> clienteService.obtenerPerfilPorUsername("noexiste"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Perfil de cliente no encontrado para el usuario: noexiste");
        }
    }

    // =========================================================================
    // 7. PRUEBAS PARA: obtenerPerfilAutenticado()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para obtenerPerfilAutenticado")
    class ObtenerPerfilAutenticadoTests {

        @Test
        @DisplayName("Debe retornar DTO mapeado para el cliente autenticado")
        void obtenerPerfilAutenticado_Exito() {
            // Given
            given(clienteRepository.findByUsuario_Username("johndoe")).willReturn(Optional.of(cliente));
            given(clienteMapper.toResponseDTO(cliente)).willReturn(responseDTO);

            // When
            ClienteResponseDTO resultado = clienteService.obtenerPerfilAutenticado("johndoe");

            // Then
            assertThat(resultado).isNotNull().isEqualTo(responseDTO);
            then(clienteRepository).should(times(1)).findByUsuario_Username("johndoe");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el cliente autenticado no existe")
        void obtenerPerfilAutenticado_NoExiste_LanzaExcepcion() {
            // Given
            given(clienteRepository.findByUsuario_Username("desconocido")).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> clienteService.obtenerPerfilAutenticado("desconocido"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Cliente no encontrado para el usuario: desconocido");
        }
    }

    // =========================================================================
    // 8. PRUEBAS PARA: actualizarPerfilAutenticado()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para actualizarPerfilAutenticado")
    class ActualizarPerfilAutenticadoTests {

        @Test
        @DisplayName("Debe actualizar exitosamente el perfil autenticado")
        void actualizarPerfilAutenticado_Exito() {
            // Given
            requestDTO.setUsername("johndoe_nuevo");
            requestDTO.setEmail("john_nuevo@email.com");
            requestDTO.setPassword("newpass123");

            given(clienteRepository.findByUsuario_Username("johndoe")).willReturn(Optional.of(cliente));
            given(usuarioRepository.existsByUsername("johndoe_nuevo")).willReturn(false);
            given(usuarioRepository.existsByEmail("john_nuevo@email.com")).willReturn(false);
            given(passwordEncoder.encode("newpass123")).willReturn("encoded_pass");
            given(clienteRepository.save(cliente)).willReturn(cliente);
            given(clienteMapper.toResponseDTO(cliente)).willReturn(responseDTO);

            // When
            ClienteResponseDTO resultado = clienteService.actualizarPerfilAutenticado("johndoe", requestDTO);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(usuario.getUsername()).isEqualTo("johndoe_nuevo");
            assertThat(usuario.getEmail()).isEqualTo("john_nuevo@email.com");

            then(passwordEncoder).should(times(1)).encode("newpass123");
            then(clienteRepository).should(times(1)).save(cliente);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el usuario a actualizar no existe")
        void actualizarPerfilAutenticado_NoExiste_LanzaExcepcion() {
            // Given
            given(clienteRepository.findByUsuario_Username("desconocido")).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> clienteService.actualizarPerfilAutenticado("desconocido", requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No se puede actualizar: Cliente no encontrado para el usuario: desconocido");

            then(clienteRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el nuevo username ya está en uso")
        void actualizarPerfilAutenticado_UsernameEnUso_LanzaExcepcion() {
            // Given
            requestDTO.setUsername("usuarioExiste");
            given(clienteRepository.findByUsuario_Username("johndoe")).willReturn(Optional.of(cliente));
            given(usuarioRepository.existsByUsername("usuarioExiste")).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> clienteService.actualizarPerfilAutenticado("johndoe", requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El nombre de usuario 'usuarioExiste' ya está en uso");

            then(clienteRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el nuevo email ya está en uso")
        void actualizarPerfilAutenticado_EmailEnUso_LanzaExcepcion() {
            // Given
            requestDTO.setEmail("emailExiste@email.com");
            given(clienteRepository.findByUsuario_Username("johndoe")).willReturn(Optional.of(cliente));
            given(usuarioRepository.existsByEmail("emailExiste@email.com")).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> clienteService.actualizarPerfilAutenticado("johndoe", requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El correo electrónico 'emailExiste@email.com' ya está registrado");

            then(clienteRepository).should(never()).save(any());
        }
    }
}
