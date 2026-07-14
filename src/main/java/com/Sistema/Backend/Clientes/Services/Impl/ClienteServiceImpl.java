package com.Sistema.Backend.Clientes.Services.Impl;

import com.Sistema.Backend.Clientes.Dto.Request.ClienteRequestDTO;
import com.Sistema.Backend.Clientes.Dto.Response.ClienteResponseDTO;
import com.Sistema.Backend.Clientes.Entity.Cliente;
import com.Sistema.Backend.Clientes.Mapper.ClienteMapper;
import com.Sistema.Backend.Clientes.Repository.ClienteRepository;
import com.Sistema.Backend.Clientes.Services.ClienteService;
import com.Sistema.Backend.Usuarios.Entity.Rol;
import com.Sistema.Backend.Usuarios.Entity.TipoRol;
import com.Sistema.Backend.Usuarios.Entity.Usuario;
import com.Sistema.Backend.Usuarios.Repository.RolRepository;
import com.Sistema.Backend.Usuarios.Repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Slf4j
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClienteMapper clienteMapper;

    public ClienteServiceImpl(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.clienteMapper = clienteMapper;
    }

    @Override
    @Transactional
    public ClienteResponseDTO registrarClientePublico(ClienteRequestDTO dto) {
        log.info("Iniciando registro público para el nuevo cliente con email: {}", dto.getEmail());

        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            log.warn("Intento de registro fallido: El username '{}' ya existe.", dto.getUsername());
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            log.warn("Intento de registro fallido: El email '{}' ya está en uso.", dto.getEmail());
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        // Buscar el Rol CLIENTE de forma segura y automática en el backend
        Rol rolCliente = rolRepository.findByNombre(TipoRol.CLIENTE)
                .orElseThrow(() -> {
                    log.error("Error crítico: El rol CLIENTE no se encuentra en la base de datos.");
                    return new RuntimeException("Error en la configuración del sistema. El rol CLIENTE no existe.");
                });

        // Construir la entidad Usuario (Manejo de accesos)
        Usuario usuario = Usuario.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .roles(Set.of(rolCliente))
                .activo(true) // Cuenta de usuario inicialmente activa
                .build();

        // Construir la entidad Cliente vinculando el Usuario
        Cliente cliente = Cliente.builder()
                .nombreCompleto(dto.getNombreCompleto())
                .telefono(dto.getTelefono())
                .direccionEntrega(dto.getDireccionEntrega())
                .usuario(usuario)
                .activo(true) // Perfil lógicamente activo
                .build();

        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("Cliente registrado con éxito en el sistema. ID asignado: {}", clienteGuardado.getId());

        return clienteMapper.toResponseDTO(clienteGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listarClientesPaginados(String filtro, Boolean activo, Pageable pageable) {
        log.info("Consultando clientes paginados con filtro: '{}' y estado activo: {}", filtro, activo);

        // Invoca la query avanzada que nos mostraste en el Repository
        Page<Cliente> clientesPage = clienteRepository.buscarClientesPaginados(filtro, activo, pageable);

        // Mapeamos cada entidad Cliente en el flujo de la página a un ClienteResponseDTO usando tu Mapper
        return clientesPage.map(clienteMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorId(Long id) {
        log.info("Buscando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("No se encontró el cliente con ID: {}", id);
                    return new RuntimeException("Cliente no encontrado con el ID especificado");
                });
        return clienteMapper.toResponseDTO(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO dto) {
        log.info("Actualizando datos del cliente con ID: {}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Usuario usuario = cliente.getUsuario();

        // Validaciones cruzadas para evitar duplicar nombres de usuario o emails al editar
        if (!usuario.getUsername().equals(dto.getUsername()) && usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está asignado a otra cuenta");
        }
        if (!usuario.getEmail().equals(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está asignado a otra cuenta");
        }

        // Actualizar datos del perfil de cliente
        cliente.setNombreCompleto(dto.getNombreCompleto());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccionEntrega(dto.getDireccionEntrega());

        // Actualizar datos de credenciales del usuario
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());

        // Si mandan una contraseña en la edición, la encriptamos y actualizamos
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            log.info("Se detectó cambio de contraseña para el cliente con ID: {}", id);
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Cliente clienteActualizado = clienteRepository.save(cliente);
        log.info("Cliente con ID: {} actualizado correctamente.", id);

        return clienteMapper.toResponseDTO(clienteActualizado);
    }

    @Override
    @Transactional
    public void cambiarEstadoActivo(Long id, boolean activo) {
        log.info("Ejecutando Soft Delete lógico. Cambiando estado 'activo' a {} para el cliente con ID: {}", activo, id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Cambiamos el estado lógico en cascada tanto en el perfil como en las credenciales
        cliente.setActivo(activo);
        cliente.getUsuario().setActivo(activo);

        clienteRepository.save(cliente);
        log.info("Soft Delete completado con éxito. Estado del cliente ID {}: {}", id, activo ? "ACTIVO" : "INACTIVO");
    }
}
