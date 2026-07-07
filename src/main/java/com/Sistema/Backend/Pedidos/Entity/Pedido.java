package com.Sistema.Backend.Pedidos.Entity;

import com.Sistema.Backend.Mesas.Entity.Mesa;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Setter
@Getter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "whatsapp_final", nullable = true, length = 10)
    private String whatsappFinal;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relación con los detalles: un pedido tiene muchos productos
    // cascade = CascadeType.ALL permite guardar el pedido y sus detalles en un solo paso
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String notas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id") // Nombre de la columna de la llave foránea en tu tabla pedidos
    private Mesa mesa;

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

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}

