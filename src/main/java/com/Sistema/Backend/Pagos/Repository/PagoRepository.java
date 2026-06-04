package com.Sistema.Backend.Pagos.Repository;

import com.Sistema.Backend.Pagos.Entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Buscar por el código público (PAG-XXXXX)
    Optional<Pago> findByCodigoTransaccion(String codigoTransaccion);

    // Buscar el pago usando el ID del pedido relacionado
    Optional<Pago> findByPedidoId(Long pedidoId);
}
