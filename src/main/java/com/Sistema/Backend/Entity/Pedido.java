package com.Sistema.Backend.Entity;

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

    @Column(name = "whatsapp_final", nullable = false, length = 4)
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
    }
}

