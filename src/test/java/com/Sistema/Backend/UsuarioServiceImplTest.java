package com.Sistema.Backend;

import com.Sistema.Backend.Exception.BadRequestException;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Usuarios.Dto.Request.UsuarioRequestDTO;
import com.Sistema.Backend.Usuarios.Dto.Response.UsuarioResponseDTO;
import com.Sistema.Backend.Usuarios.Entity.Rol;
import com.Sistema.Backend.Usuarios.Entity.TipoRol;
import com.Sistema.Backend.Usuarios.Entity.Usuario;
import com.Sistema.Backend.Usuarios.Mapper.UsuarioMapper;
import com.Sistema.Backend.Usuarios.Repository.RolRepository;
import com.Sistema.Backend.Usuarios.Repository.UsuarioRepository;
import com.Sistema.Backend.Usuarios.Services.Impl.UsuarioServiceImpl;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;
    private Rol rolAdmin;
    private UsuarioRequestDTO usuarioRequestDTO;
    private UsuarioResponseDTO usuarioResponseDTO;

    @BeforeEach
    void setUp() {
        rolAdmin = new Rol();
        rolAdmin.setId(1L);
        rolAdmin.setNombre(TipoRol.ADMIN);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setPassword("encodedPassword123");
        usuario.setActivo(true);
        usuario.setRoles(new HashSet<>(Set.of(rolAdmin)));

        usuarioRequestDTO = new UsuarioRequestDTO();
        usuarioRequestDTO.setUsername("admin");
        usuarioRequestDTO.setPassword("rawPassword123");
        usuarioRequestDTO.setRoles(Set.of("ADMIN"));

        usuarioResponseDTO = new UsuarioResponseDTO();
        usuarioResponseDTO.setId(1L);
        usuarioResponseDTO.setUsername("admin");
        usuarioResponseDTO.setActivo(true);
    }

    // =========================================================================
    // 1. PRUEBAS PARA: registrarUsuario()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para registro de usuarios")
    class RegistrarUsuarioTests {

        @Test
        @DisplayName("Debe registrar exitosamente un usuario con contraseña encriptada y roles asignados")
        void registrarUsuario_Exito() {
            // Given
            given(usuarioRepository.existsByUsername("admin")).willReturn(false);
            given(usuarioMapper.toEntity(usuarioRequestDTO)).willReturn(usuario);
            given(passwordEncoder.encode("rawPassword123")).willReturn("encodedPassword123");
            given(rolRepository.findByNombre(TipoRol.ADMIN)).willReturn(Optional.of(rolAdmin));
            given(usuarioRepository.save(usuario)).willReturn(usuario);
            given(usuarioMapper.toResponse(usuario)).willReturn(usuarioResponseDTO);

            // When
            UsuarioResponseDTO resultado = usuarioService.registrarUsuario(usuarioRequestDTO);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getUsername()).isEqualTo("admin");
            assertThat(usuario.getPassword()).isEqualTo("encodedPassword123");
            assertThat(usuario.isActivo()).isTrue();
            assertThat(usuario.getRoles()).contains(rolAdmin);

            then(usuarioRepository).should(times(1)).existsByUsername("admin");
            then(passwordEncoder).should(times(1)).encode("rawPassword123");
            then(rolRepository).should(times(1)).findByNombre(TipoRol.ADMIN);
            then(usuarioRepository).should(times(1)).save(usuario);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el username ya está en uso")
        void registrarUsuario_UsernameDuplicado_LanzaExcepcion() {
            // Given
            given(usuarioRepository.existsByUsername("admin")).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> usuarioService.registrarUsuario(usuarioRequestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El nombre de usuario ya está en uso");

            then(usuarioRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el rol proporcionado no existe en la BD")
        void registrarUsuario_RolInexistente_LanzaExcepcion() {
            // Given
            usuarioRequestDTO.setRoles(Set.of("ROLE_INEXISTENTE"));

            given(usuarioRepository.existsByUsername("admin")).willReturn(false);
            given(usuarioMapper.toEntity(usuarioRequestDTO)).willReturn(usuario);
            given(passwordEncoder.encode("rawPassword123")).willReturn("encodedPassword123");

            // When / Then
            assertThatThrownBy(() -> usuarioService.registrarUsuario(usuarioRequestDTO))
                    .isInstanceOf(IllegalArgumentException.class); // Provocado por TipoRol.valueOf

            then(usuarioRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException cuando el rol existe en TipoRol pero no se encuentra en el repositorio")
        void registrarUsuario_RolNoPersistido_LanzaExcepcion() {
            // Given
            given(usuarioRepository.existsByUsername("admin")).willReturn(false);
            given(usuarioMapper.toEntity(usuarioRequestDTO)).willReturn(usuario);
            given(passwordEncoder.encode("rawPassword123")).willReturn("encodedPassword123");
            given(rolRepository.findByNombre(TipoRol.ADMIN)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> usuarioService.registrarUsuario(usuarioRequestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Error: El rol ADMIN no existe en la BD.");

            then(usuarioRepository).should(never()).save(any());
        }
    }

    // =========================================================================
    // 2. PRUEBAS PARA: listarUsuariosPaginados()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para búsqueda paginada")
    class ListarUsuariosPaginadosTests {

        @Test
        @DisplayName("Debe retornar la página de usuarios filtrados correctamente")
        void listarUsuariosPaginados_Exito() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Usuario> pageUsuarios = new PageImpl<>(List.of(usuario));

            // Si tu servicio hace username.trim(), el repositorio recibirá "admin"
            given(usuarioRepository.buscarTodosParaAdminPaginado("admin", true, pageable))
                    .willReturn(pageUsuarios);
            given(usuarioMapper.toResponse(usuario)).willReturn(usuarioResponseDTO);

            // When
            Page<UsuarioResponseDTO> resultado = usuarioService.listarUsuariosPaginados(" admin ", true, pageable);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1).contains(usuarioResponseDTO);
            then(usuarioRepository).should(times(1)).buscarTodosParaAdminPaginado("admin", true, pageable);
        }

        @Test
        @DisplayName("Debe convertir filtros de búsqueda vacíos o nulos a null para la consulta nativa")
        void listarUsuariosPaginados_FiltrosVacios_PasaNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Usuario> pageUsuarios = new PageImpl<>(List.of(usuario));

            given(usuarioRepository.buscarTodosParaAdminPaginado(eq(null), eq(null), eq(pageable)))
                    .willReturn(pageUsuarios);
            given(usuarioMapper.toResponse(usuario)).willReturn(usuarioResponseDTO);

            // When
            Page<UsuarioResponseDTO> resultado = usuarioService.listarUsuariosPaginados("   ", null, pageable);

            // Then
            assertThat(resultado).isNotNull();
            then(usuarioRepository).should(times(1)).buscarTodosParaAdminPaginado(eq(null), eq(null), eq(pageable));
        }
    }

    // =========================================================================
    // 3. PRUEBAS PARA: obtenerPorId(), cambiarEstadoUsuario() y eliminarUsuario()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para operaciones individuales por ID")
    class OperacionesPorIdTests {

        @Test
        @DisplayName("Debe retornar Optional con UsuarioResponseDTO si el ID existe")
        void obtenerPorId_Existe_RetornaDTO() {
            // Given
            given(usuarioRepository.findById(1L)).willReturn(Optional.of(usuario));
            given(usuarioMapper.toResponse(usuario)).willReturn(usuarioResponseDTO);

            // When
            Optional<UsuarioResponseDTO> resultado = usuarioService.obtenerPorId(1L);

            // Then
            assertThat(resultado).isPresent().contains(usuarioResponseDTO);
            then(usuarioRepository).should(times(1)).findById(1L);
        }

        @Test
        @DisplayName("Debe cambiar el estado del usuario correctamente")
        void cambiarEstadoUsuario_Exito() {
            // Given
            given(usuarioRepository.findById(1L)).willReturn(Optional.of(usuario));

            // When
            usuarioService.cambiarEstadoUsuario(1L, false);

            // Then
            assertThat(usuario.isActivo()).isFalse();
            then(usuarioRepository).should(times(1)).save(usuario);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException al intentar cambiar estado de un usuario inexistente")
        void cambiarEstadoUsuario_NoExiste_LanzaExcepcion() {
            // Given
            given(usuarioRepository.findById(99L)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> usuarioService.cambiarEstadoUsuario(99L, false))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Usuario no encontrado con ID: 99");

            then(usuarioRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Debe eliminar físicamente el usuario si existe")
        void eliminarUsuario_Exito() {
            // Given
            given(usuarioRepository.existsById(1L)).willReturn(true);

            // When
            usuarioService.eliminarUsuario(1L);

            // Then
            then(usuarioRepository).should(times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException al intentar eliminar un usuario que no existe")
        void eliminarUsuario_NoExiste_LanzaExcepcion() {
            // Given
            given(usuarioRepository.existsById(99L)).willReturn(false);

            // When / Then
            assertThatThrownBy(() -> usuarioService.eliminarUsuario(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Usuario no encontrado con ID: 99");

            then(usuarioRepository).should(never()).deleteById(any());
        }
    }
}
