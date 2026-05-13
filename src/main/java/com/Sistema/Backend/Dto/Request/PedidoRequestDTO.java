package com.Sistema.Backend.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PedidoRequestDTO {

    @NotBlank(message = "El WhatsApp es obligatorio")
    @Size(min = 4, max = 4, message = "Deben ser exactamente los últimos 4 dígitos")
    private String whatsappFinal;

    private String nombreCliente;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    private List<ItemPedidoRequestDTO> items;
}
