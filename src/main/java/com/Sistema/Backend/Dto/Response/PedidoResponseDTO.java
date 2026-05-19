package com.Sistema.Backend.Dto.Response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoResponseDTO {

    private Long id;
    private String nombreCliente;
    private String whatsappFinal;
    private BigDecimal total;
    private String estado;
    private LocalDateTime fechaCreacion;
    private List<ItemResponseDTO> detalles;
}
