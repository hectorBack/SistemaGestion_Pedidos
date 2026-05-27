package com.Sistema.Backend.Pedidos.Mapper;

import com.Sistema.Backend.Pedidos.Dto.Response.ItemResponseDTO;
import com.Sistema.Backend.Pedidos.Dto.Response.PedidoResponseDTO;
import com.Sistema.Backend.Pedidos.Entity.DetallePedido;
import com.Sistema.Backend.Pedidos.Entity.Pedido;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    public PedidoResponseDTO toResponseDTO(Pedido pedido) {
        if (pedido == null) return null;

        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setNombreCliente(pedido.getNombreCliente());
        dto.setWhatsappFinal(pedido.getWhatsappFinal());
        dto.setTotal(pedido.getTotal());
        dto.setEstado(pedido.getEstado().name());
        dto.setFechaCreacion(pedido.getFechaCreacion());

        dto.setDetalles(pedido.getDetalles().stream()
                .map(this::toItemResponseDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    private ItemResponseDTO toItemResponseDTO(DetallePedido detalle) {
        ItemResponseDTO dto = new ItemResponseDTO();
        dto.setNombreProducto(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setNotas(detalle.getNotasPersonalizacion());
        return dto;
    }
}
