package com.Sistema.Backend.Clientes.Dto.Response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponseDTO {
    private Long id;
    private Long usuarioId;
    private String username;
    private String email;
    private String nombreCompleto;
    private String telefono;
    private String direccionEntrega;
    private boolean activo;
    private Set<String> roles; // Viene del Set<Rol> o Set<String> del Usuario
}
