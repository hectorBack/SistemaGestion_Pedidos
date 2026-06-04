package com.Sistema.Backend.Pagos.Dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagoRequestDTO {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long pedidoId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser un valor mayor a cero")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio")
    private String metodoPago; // Recibimos el String (EFECTIVO, TARJETA, etc.) para validar internamente

    private String referenciaExterna; // Opcional (Folio de transferencia, ID pasarela)

    private String notas; // Opcional (Ej: "Paga con billete de $500")
}
