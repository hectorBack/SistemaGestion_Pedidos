package com.Sistema.Backend.Pedidos.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PedidoRequestDTO {

    private String whatsappFinal;

    private String nombreCliente;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    private List<ItemPedidoRequestDTO> items;

    private Long promocionId; // Para saber qué promoción general aplicó

    private String notas;

    private Long mesaId;
}
