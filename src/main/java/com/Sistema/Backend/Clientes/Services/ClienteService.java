package com.Sistema.Backend.Clientes.Services;

import com.Sistema.Backend.Clientes.Dto.Request.ClienteRequestDTO;
import com.Sistema.Backend.Clientes.Dto.Response.ClienteResponseDTO;
import com.Sistema.Backend.Clientes.Entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteService {

    // Registro público desde el formulario (asigna rol CLIENTE automáticamente)
    ClienteResponseDTO registrarClientePublico(ClienteRequestDTO dto);

    // Búsqueda paginada con filtros en tiempo real para el panel de administración
    Page<ClienteResponseDTO> listarClientesPaginados(String filtro, Boolean activo, Pageable pageable);

    // Obtener un cliente específico por su ID
    ClienteResponseDTO obtenerPorId(Long id);

    // Modificar datos del perfil o cuenta desde la administración
    ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO dto);

    // Alternar estado (Soft Delete lógico)
    void cambiarEstadoActivo(Long id, boolean activo);

    ClienteResponseDTO obtenerPerfilPorUsername(String username);

    ClienteResponseDTO obtenerPerfilAutenticado(String username);

    ClienteResponseDTO actualizarPerfilAutenticado(String username, ClienteRequestDTO dto);
}
