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

    @NotBlank(message = "El WhatsApp es obligatorio")
    @Size(min = 10, max = 10, message = "Deben ser exactamente los últimos 10 dígitos")
    private String whatsappFinal;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    private List<ItemPedidoRequestDTO> items;

    private Long promocionId; // Para saber qué promoción general aplicó
}
