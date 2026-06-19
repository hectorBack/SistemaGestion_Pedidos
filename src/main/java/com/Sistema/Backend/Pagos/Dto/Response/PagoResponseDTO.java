package com.Sistema.Backend.Pagos.Dto.Response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class PagoResponseDTO {

    private Long id;
    private String codigoTransaccion;
    private String pedidoCodigo; // Exponemos el código del pedido (ej: PED-8C473) en vez del ID de la BD
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private String referenciaExterna;
    private LocalDateTime fechaPago;
    private String notas;
}
