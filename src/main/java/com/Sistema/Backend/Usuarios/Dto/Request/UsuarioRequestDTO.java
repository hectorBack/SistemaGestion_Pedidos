package com.Sistema.Backend.Usuarios.Dto.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UsuarioRequestDTO {

    private String username;
    private String password; // Se usará principalmente al crear; opcional al editar
    private String email;
    private boolean activo;
    private Set<String> roles; // Nombres de los roles: ["ADMIN", "MESERO", etc.]
}
