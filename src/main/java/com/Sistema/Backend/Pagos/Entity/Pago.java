package com.Sistema.Backend.Pagos.Entity;

import com.Sistema.Backend.Pedidos.Entity.Pedido;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagos")
@Data
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_transaccion", nullable = false, unique = true, length = 30)
    private String codigoTransaccion;

    // Relación directa con el pedido.
    // Usamos FetchType.LAZY para no sobrecargar la BD cuando listemos pagos de forma masiva.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    @Column(name = "referencia_externa", length = 100)
    private String referenciaExterna;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @PrePersist
    protected void onCreate() {
        this.fechaPago = LocalDateTime.now();
        if (this.codigoTransaccion == null) {
            // Generamos un código único para el pago similar al del pedido
            this.codigoTransaccion = "PAG-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        }
    }
}
