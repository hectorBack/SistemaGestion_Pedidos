package com.Sistema.Backend.Usuarios.Controllers;

import com.Sistema.Backend.Config.ErrorRespuestaDTO;
import com.Sistema.Backend.Usuarios.Dto.Request.LoginRequestDTO;
import com.Sistema.Backend.Usuarios.Dto.Request.RegistroUsuarioRequestDTO;
import com.Sistema.Backend.Usuarios.Dto.Response.JwtResponseDTO;
import com.Sistema.Backend.Usuarios.Entity.Rol;
import com.Sistema.Backend.Usuarios.Entity.TipoRol;
import com.Sistema.Backend.Usuarios.Entity.Usuario;
import com.Sistema.Backend.Usuarios.Repository.RolRepository;
import com.Sistema.Backend.Usuarios.Repository.UsuarioRepository;
import com.Sistema.Backend.Usuarios.Security.JwtUtils;
import com.Sistema.Backend.Usuarios.Security.Services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    // 1. ENDPOINT DE INICIO DE SESIÓN (LOGIN)
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponseDTO(jwt, "Bearer", userDetails.getId(),
                    userDetails.getUsername(), userDetails.getEmail(), roles));

        } catch (DisabledException e) {
            // Retornamos DIRECTAMENTE tu ErrorRespuestaDTO con un 403 Forbidden seguro
            ErrorRespuestaDTO errorDto = new ErrorRespuestaDTO(
                    LocalDateTime.now(),
                    "CUENTA_DESHABILITADA",
                    "Tu cuenta se encuentra temporalmente deshabilitada. Contacta al administrador."
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDto);

        } catch (BadCredentialsException e) {
            // Credenciales incorrectas comunes (401 Unauthorized)
            ErrorRespuestaDTO errorDto = new ErrorRespuestaDTO(
                    LocalDateTime.now(),
                    "CREDANCIALES_INVALIDAS",
                    "El nombre de usuario o la contraseña son incorrectos."
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);

        } catch (Exception e) {
            System.out.println("ERROR CRÍTICO INESPERADO EN LOGIN: " + e.getMessage());
            e.printStackTrace();

            ErrorRespuestaDTO errorDto = new ErrorRespuestaDTO(
                    LocalDateTime.now(),
                    "ERROR_INTERNO",
                    "Ocurrió un error inesperado en el servidor."
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistroUsuarioRequestDTO signUpRequest) {
        if (usuarioRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: ¡El Username ya está en uso!"));
        }

        if (usuarioRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: ¡El Email ya está en uso!"));
        }

        // Creamos la nueva cuenta de usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(signUpRequest.getUsername());
        usuario.setEmail(signUpRequest.getEmail());
        usuario.setPassword(encoder.encode(signUpRequest.getPassword())); // Encriptamos password
        usuario.setActivo(true);

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Rol> roles = new HashSet<>();

        // CAMBIO 1: Si no se especifican roles, el rol por defecto ahora es CLIENTE por seguridad
        if (strRoles == null || strRoles.isEmpty()) {
            Rol clienteRol = rolRepository.findByNombre(TipoRol.CLIENTE)
                    .orElseThrow(() -> new RuntimeException("Error: El Rol CLIENTE no fue encontrado en la BD."));
            roles.add(clienteRol);
        } else {
            strRoles.forEach(role -> {
                switch (role.toUpperCase()) {
                    case "ADMIN":
                        Rol adminRol = rolRepository.findByNombre(TipoRol.ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: El Rol ADMIN no fue encontrado."));
                        roles.add(adminRol);
                        break;
                    case "COCINERO":
                        Rol cocineroRol = rolRepository.findByNombre(TipoRol.COCINERO)
                                .orElseThrow(() -> new RuntimeException("Error: El Rol COCINERO no fue encontrado."));
                        roles.add(cocineroRol);
                        break;
                    case "MESERO":
                        Rol meseroRol = rolRepository.findByNombre(TipoRol.MESERO)
                                .orElseThrow(() -> new RuntimeException("Error: El Rol MESERO no fue encontrado."));
                        roles.add(meseroRol);
                        break;
                    // CAMBIO 2: Agregamos el caso explícito para CLIENTE
                    case "CLIENTE":
                        Rol clienteRol = rolRepository.findByNombre(TipoRol.CLIENTE)
                                .orElseThrow(() -> new RuntimeException("Error: El Rol CLIENTE no fue encontrado."));
                        roles.add(clienteRol);
                        break;
                    // Si mandan un texto basura, cae a CLIENTE para no otorgar permisos de staff por error
                    default:
                        Rol porDefectoRol = rolRepository.findByNombre(TipoRol.CLIENTE)
                                .orElseThrow(() -> new RuntimeException("Error: El Rol CLIENTE no fue encontrado."));
                        roles.add(porDefectoRol);
                }
            });
        }

        usuario.setRoles(roles);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("message", "¡Usuario registrado exitosamente!"));
    }
}
