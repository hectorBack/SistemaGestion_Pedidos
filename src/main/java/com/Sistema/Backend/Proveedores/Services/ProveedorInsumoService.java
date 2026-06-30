package com.Sistema.Backend.Proveedores.Services;

import com.Sistema.Backend.Proveedores.Dto.Request.ProveedorInsumoRequestDTO;
import com.Sistema.Backend.Proveedores.Dto.Response.ProveedorInsumoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProveedorInsumoService {

    Page<ProveedorInsumoResponseDTO> listarPorProveedor(Long proveedorId, Boolean activo, Pageable pageable);
    List<ProveedorInsumoResponseDTO> obtenerProveedoresPorInsumo(Long insumoId);
    ProveedorInsumoResponseDTO asignarOActualizarCosto(ProveedorInsumoRequestDTO dto);
    void eliminarRelacion(Long id); // Soft Delete del insumo del proveedor
}
