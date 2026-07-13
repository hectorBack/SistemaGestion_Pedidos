package com.Sistema.Backend.Usuarios.Mapper;

import com.Sistema.Backend.Usuarios.Dto.Response.UsuarioResponseDTO;
import com.Sistema.Backend.Usuarios.Entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(usuario.getId());
        response.setUsername(usuario.getUsername());
        response.setEmail(usuario.getEmail());
        response.setActivo(usuario.isActivo());

        // Mapeamos el Set<Rol> a un Set<String> con los nombres de los roles
        response.setRoles(usuario.getRoles().stream()
                .map(rol -> rol.getNombre().name())
                .collect(Collectors.toSet()));

        return response;
    }
}
