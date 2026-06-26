package com.Sistema.Backend.Pedidos.Dto.Request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ComandaMesaRequestDTO {

    @NotEmpty(message = "La comanda debe tener al menos un producto")
    private List<ItemPedidoRequestDTO> items;

    private String notas;

    private Long promocionId;
}
