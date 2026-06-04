package com.Sistema.Backend.Pagos.Entity;

public enum EstadoPago {
    PENDIENTE,   // El pago se inició (ej: el cliente eligió pagar con transferencia o tarjeta pero no ha completado el flujo).
    APROBADO,    // El dinero ya está en nuestra cuenta de forma segura. El pedido ya puede pasar a la cocina.
    RECHAZADO,   // La pasarela de pago denegó la tarjeta o fondos insuficientes.
    REEMBOLSADO, // El dinero se le devolvió al cliente (por cancelación del pedido o error en cocina).
    FALLIDO      // Ocurrió un error técnico durante la transacción (ej: timeout con el banco o webhook roto).
}