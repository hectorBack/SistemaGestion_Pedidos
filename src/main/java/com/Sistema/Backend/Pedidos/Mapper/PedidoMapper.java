package com.Sistema.Backend.Pedidos.Mapper;

import com.Sistema.Backend.Pedidos.Dto.Response.ItemResponseDTO;
import com.Sistema.Backend.Pedidos.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Pedidos.Entity.DetallePedido;
import com.Sistema.Backend.Pedidos.Entity.Pedido;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    public PedidoResponseDTO toResponseDTO(Pedido pedido) {
        if (pedido == null) return null;

        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setCodigo(pedido.getCodigo());
        dto.setNombreCliente(pedido.getNombreCliente());
        dto.setWhatsappFinal(pedido.getWhatsappFinal());
        dto.setTotal(pedido.getTotal());
        dto.setEstado(pedido.getEstado().name());
        dto.setFechaCreacion(pedido.getFechaCreacion());
        dto.setFechaActualizacion(pedido.getFechaActualizacion());
        dto.setNotas(pedido.getNotas());

        // 1. PROMOCIÓN A NIVEL DE PEDIDO (GLOBAL)
        if (pedido.getPromocion() != null) {
            dto.setNombrePromocion(pedido.getPromocion().getNombre());
        }

        dto.setDetalles(pedido.getDetalles().stream()
                .map(this::toItemResponseDTO)
                .collect(Collectors.toList()));

        if (pedido.getMesa() != null) {
            dto.setMesaId(pedido.getMesa().getId());
            dto.setNumeroMesa(pedido.getMesa().getNumero()); // Evita errores de compilación
        }

        return dto;
    }

    private ItemResponseDTO toItemResponseDTO(DetallePedido detalle) {
        ItemResponseDTO dto = new ItemResponseDTO();
        dto.setNombreProducto(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setNotas(detalle.getNotas());

        // 🌟 2. PROMOCIÓN A NIVEL DE ÍTEM (PRODUCTO ESPECÍFICO)
        // Evaluamos si el detalle tiene promoción asociada directamente o a través del producto
        if (detalle.getPromocion() != null) {
            dto.setPromocionNombre(detalle.getPromocion().getNombre());
        } else if (detalle.getProducto() != null && detalle.getProducto().getPromocion() != null) {
            dto.setPromocionNombre(detalle.getProducto().getPromocion().getNombre());
        }

        // 🌶️ SOLUCIÓN: Forzamos la inicialización limpia de la lista para el JSON de respuesta
        if (detalle.getSabores() != null) {
            dto.setSabores(new ArrayList<>(detalle.getSabores()));
        } else {
            dto.setSabores(new ArrayList<>());
        }
        return dto;
    }
}
