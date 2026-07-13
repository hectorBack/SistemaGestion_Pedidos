package com.Sistema.Backend.Usuarios.Services;

import com.Sistema.Backend.Usuarios.Dto.Request.UsuarioRequestDTO;
import com.Sistema.Backend.Usuarios.Dto.Response.UsuarioResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UsuarioService {
    // Obtener todos los usuarios de forma paginada
    Page<UsuarioResponseDTO> listarUsuariosPaginados(Pageable pageable);

    UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO dto);

    // Buscar un usuario por ID expuesto como Response
    Optional<UsuarioResponseDTO> obtenerPorId(Long id);

    // Cambiar el estado del usuario (Activar / Desactivar)
    void cambiarEstadoUsuario(Long id, boolean activo);

    // Eliminar físicamente a un usuario (Opcional, usualmente se prefiere desactivar)
    void eliminarUsuario(Long id);
}