package com.Sistema.Backend.Config;

import com.Sistema.Backend.Usuarios.Dto.Request.UsuarioRequestDTO;
import com.Sistema.Backend.Usuarios.Entity.Rol;
import com.Sistema.Backend.Usuarios.Entity.TipoRol;
import com.Sistema.Backend.Usuarios.Repository.RolRepository;
import com.Sistema.Backend.Usuarios.Repository.UsuarioRepository;
import com.Sistema.Backend.Usuarios.Services.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioService usuarioService;

    public DataInitializer(UsuarioRepository usuarioRepository, RolRepository rolRepository, UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Asegurar que existan los roles básicos en la base de datos
        inicializarRoles();

        // 2. Crear el usuario Administrador por defecto si no existe
        inicializarAdminPorDefecto();
    }

    private void inicializarRoles() {
        // 1. Recorremos directamente los valores definidos en tu Enum 'TipoRol'
        // (Asegúrate de incluir los que necesites o recorrer TipoRol.values())
        List<TipoRol> rolesRequeridos = List.of(
                TipoRol.ADMIN,
                TipoRol.MESERO,
                TipoRol.COCINERO,
                TipoRol.CLIENTE
        );

        for (TipoRol tipoRol : rolesRequeridos) {
            // 2. Usamos .isEmpty() sobre el Optional<Rol> devuelto por findByNombre
            if (rolRepository.findByNombre(tipoRol).isEmpty()) {
                Rol rol = new Rol();
                rol.setNombre(tipoRol); // Asignamos el valor del Enum
                rolRepository.save(rol);
                System.out.println("Rol creado en BD: " + tipoRol);
            }
        }
    }

    private void inicializarAdminPorDefecto() {
        String usernameAdmin = "admin";

        // Si no existe un usuario con username 'admin', lo creamos
        if (!usuarioRepository.existsByUsername(usernameAdmin)) {
            UsuarioRequestDTO adminDTO = new UsuarioRequestDTO();
            adminDTO.setUsername(usernameAdmin);
            adminDTO.setPassword("admin123"); // Contraseña por defecto (el cliente la puede cambiar)
            adminDTO.setEmail("admin@sistema.com");
            adminDTO.setActivo(true); // Activo por defecto
            adminDTO.setRoles(Set.of("ADMIN")); // Le asignamos el rol ADMIN

            // Usamos el servicio existente para guardar, garantizando que encodee la contraseña
            usuarioService.registrarUsuario(adminDTO);

            System.out.println(" Usuario 'admin' por defecto creado exitosamente con contraseña 'admin123'.");
        } else {
            System.out.println(" El usuario 'admin' ya existe en la base de datos.");
        }
    }
}
