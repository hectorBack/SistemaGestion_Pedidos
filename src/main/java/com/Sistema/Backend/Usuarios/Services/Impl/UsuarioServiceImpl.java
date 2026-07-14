package com.Sistema.Backend.Usuarios.Services.Impl;

import com.Sistema.Backend.Usuarios.Dto.Request.UsuarioRequestDTO;
import com.Sistema.Backend.Usuarios.Dto.Response.UsuarioResponseDTO;
import com.Sistema.Backend.Usuarios.Entity.Rol;
import com.Sistema.Backend.Usuarios.Entity.TipoRol;
import com.Sistema.Backend.Usuarios.Entity.Usuario;
import com.Sistema.Backend.Usuarios.Mapper.UsuarioMapper;
import com.Sistema.Backend.Usuarios.Repository.RolRepository;
import com.Sistema.Backend.Usuarios.Repository.UsuarioRepository;
import com.Sistema.Backend.Usuarios.Services.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository, UsuarioMapper usuarioMapper, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarUsuariosPaginados(String username, Boolean activo, Pageable pageable) {
        // 🌟 Log de SLF4J idéntico al patrón de categorías
        log.info("Búsqueda paginada de usuarios - Filtro username: '{}' - Filtro Estado (Activo): {} - Página: {} - Tamaño: {}",
                username, activo, pageable.getPageNumber(), pageable.getPageSize());

        String filtroUsername = (username != null && !username.trim().isEmpty()) ? username : null;

        // Pasamos los filtros directamente a la query nativa del repositorio
        Page<Usuario> usuarios = usuarioRepository.buscarTodosParaAdminPaginado(filtroUsername, activo, pageable);

        return usuarios.map(usuarioMapper::toResponse);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO dto) {
        log.info("Registrando un nuevo usuario en el sistema con username: {}", dto.getUsername());

        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        // Convertir datos básicos usando el mapper corregido
        Usuario usuario = usuarioMapper.toEntity(dto);

        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setActivo(true);

        // 2. ASIGNAR ROLES DESDE LA BASE DE DATOS
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            Set<Rol> rolesPersistidos = dto.getRoles().stream()
                    .map(nombreRolStr -> {
                        // Convertimos el String al Enum correspondiente
                        TipoRol nombreEnum = TipoRol.valueOf(nombreRolStr.toUpperCase());

                        // Buscamos el rol real que ya existe en la base de datos
                        return rolRepository.findByNombre(nombreEnum)
                                .orElseThrow(() -> new RuntimeException("Error: El rol " + nombreRolStr + " no existe en la BD."));
                    })
                    .collect(Collectors.toSet());

            // Le asignamos los roles reales administrados por JPA/Hibernate
            usuario.setRoles(rolesPersistidos);
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente con ID: {}", usuarioGuardado.getId());

        return usuarioMapper.toResponse(usuarioGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDTO> obtenerPorId(Long id) {
        log.info("Buscando usuario por ID: {}", id);
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toResponse);
    }

    @Override
    @Transactional
    public void cambiarEstadoUsuario(Long id, boolean activo) {
        log.info("Cambiando estado de actividad del usuario con ID: {} a activo={}", id, activo);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se pudo cambiar el estado. Usuario con ID {} no encontrado.", id);
                    return new RuntimeException("Usuario no encontrado con ID: " + id);
                });

        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
        log.info("Estado del usuario con ID {} actualizado correctamente.", id);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        log.warn("Se ha solicitado la eliminación física del usuario con ID: {}", id);
        if (!usuarioRepository.existsById(id)) {
            log.error("No se pudo eliminar. Usuario con ID {} no existe.", id);
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
        log.info("Usuario con ID {} eliminado exitosamente del sistema.", id);
    }
}
