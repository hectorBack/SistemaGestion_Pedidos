package com.Sistema.Backend.Usuarios.Services.Impl;

import com.Sistema.Backend.Usuarios.Dto.Response.UsuarioResponseDTO;
import com.Sistema.Backend.Usuarios.Entity.Usuario;
import com.Sistema.Backend.Usuarios.Mapper.UsuarioMapper;
import com.Sistema.Backend.Usuarios.Repository.UsuarioRepository;
import com.Sistema.Backend.Usuarios.Services.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarUsuariosPaginados(Pageable pageable) {
        log.info("Solicitando lista de usuarios paginada. Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Usuario> usuariosPage = usuarioRepository.findAll(pageable);

        // Mapeamos de forma óptima cada elemento de la página de Entidad a DTO
        return usuariosPage.map(usuarioMapper::toResponse);
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
