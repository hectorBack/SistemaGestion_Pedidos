package com.Sistema.Backend.Pedidos.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "whatsapp_final", nullable = false, length = 10)
    private String whatsappFinal;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Relación con los detalles: un pedido tiene muchos productos
    // cascade = CascadeType.ALL permite guardar el pedido y sus detalles en un solo paso
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();

        if (this.codigo == null) {
            this.codigo = generarCodigoUnico();
        }
    }

    private String generarCodigoUnico() {
        // Ejemplo resultado: PED-5A2C8
        String uuidCorto = java.util.UUID.randomUUID().toString()
                .substring(0, 5)
                .toUpperCase();
        return "PED-" + uuidCorto;
    }
}

