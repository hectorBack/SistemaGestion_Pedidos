package com.Sistema.Backend.Pagos.Mapper;

import com.Sistema.Backend.Pagos.Dto.Request.PagoRequestDTO;
import com.Sistema.Backend.Pagos.Dto.Response.PagoResponseDTO;
import com.Sistema.Backend.Pagos.Entity.MetodoPago;
import com.Sistema.Backend.Pagos.Entity.Pago;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public Pago toEntity(PagoRequestDTO dto) {
        if (dto == null) return null;

        Pago pago = new Pago();
        pago.setMonto(dto.getMonto());
        pago.setMetodoPago(MetodoPago.valueOf(dto.getMetodoPago().toUpperCase()));
        pago.setReferenciaExterna(dto.getReferenciaExterna());
        pago.setNotas(dto.getNotas());
        // El pedido, el estado inicial y el código se gestionan en el Service/PrePersist
        return pago;
    }

    public PagoResponseDTO toResponseDTO(Pago pago) {
        if (pago == null) return null;

        PagoResponseDTO dto = new PagoResponseDTO();
        dto.setId(pago.getId());
        dto.setCodigoTransaccion(pago.getCodigoTransaccion());
        dto.setMonto(pago.getMonto());
        dto.setMetodoPago(pago.getMetodoPago().name());
        dto.setEstado(pago.getEstado().name());
        dto.setReferenciaExterna(pago.getReferenciaExterna());
        dto.setFechaPago(pago.getFechaPago());
        dto.setNotas(pago.getNotas());

        // Mapeamos el código público del pedido en lugar del ID técnico
        if (pago.getPedido() != null) {
            dto.setPedidoCodigo(pago.getPedido().getCodigo());
        }

        return dto;
    }
}
