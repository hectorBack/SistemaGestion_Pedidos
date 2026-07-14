package com.Sistema.Backend.Clientes.Mapper;

import com.Sistema.Backend.Clientes.Dto.Response.ClienteResponseDTO;
import com.Sistema.Backend.Clientes.Entity.Cliente;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClienteMapper {

    public ClienteResponseDTO toResponseDTO(Cliente cliente) {
        if (cliente == null) return null;

        // Extraemos los roles mapeando tu entidad Rol a String si aplica
        Set<String> rolesNombre = cliente.getUsuario().getRoles().stream()
                .map(rol -> rol.getNombre().toString()) // Ajusta según cómo manejes tus Roles en Usuario
                .collect(Collectors.toSet());

        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .usuarioId(cliente.getUsuario().getId())
                .username(cliente.getUsuario().getUsername())
                .email(cliente.getUsuario().getEmail())
                .nombreCompleto(cliente.getNombreCompleto())
                .telefono(cliente.getTelefono())
                .direccionEntrega(cliente.getDireccionEntrega())
                .activo(cliente.isActivo())
                .roles(rolesNombre)
                .build();
    }
}
