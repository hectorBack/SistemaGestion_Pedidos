package com.Sistema.Backend.Pedidos.Dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemPedidoRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @Min(value = 1, message = "La cantidad mínima es 1")
    private int cantidad;

    // Aquí capturamos el "Sin cebolla", etc.
    private String notas;
}
