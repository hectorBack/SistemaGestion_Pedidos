package com.Sistema.Backend.Pedidos.Dto.Response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class PedidoResponseDTO {

    private Long id;
    private String codigo;
    private String nombreCliente;
    private String whatsappFinal;
    private BigDecimal total;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private List<ItemResponseDTO> detalles;
    private String notas;
    private Long mesaId;       // ID por si necesitas hacer consultas en Vue
    private String numeroMesa; // El número físico (ej: Mesa 3) para mostrar en pantalla

    // Campo para la promoción general del pedido entero
    private String nombrePromocion;
}
