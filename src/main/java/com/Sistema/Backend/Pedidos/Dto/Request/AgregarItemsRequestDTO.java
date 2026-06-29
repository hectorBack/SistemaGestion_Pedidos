package com.Sistema.Backend.Pedidos.Dto.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AgregarItemsRequestDTO {

    private List<ItemPedidoRequestDTO> nuevosItems; // La lista con el producto_id, cantidad y notas
}
