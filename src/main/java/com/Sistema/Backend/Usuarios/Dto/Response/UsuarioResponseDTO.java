package com.Sistema.Backend.Usuarios.Dto.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UsuarioResponseDTO {

    private Long id;
    private String username;
    private String email;
    private boolean activo;
    private Set<String> roles;
}
