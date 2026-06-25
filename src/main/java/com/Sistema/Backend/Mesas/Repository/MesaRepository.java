package com.Sistema.Backend.Mesas.Repository;

import com.Sistema.Backend.Mesas.Entity.EstadoMesa;
import com.Sistema.Backend.Mesas.Entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {

    // Buscar por el identificador único (útil para validaciones antes de guardar)
    Optional<Mesa> findByNumero(String numero);

    // Filtrar rápidamente las mesas por su estado operativo actual
    List<Mesa> findByEstado(EstadoMesa estado);

    // Verificar si ya existe un número registrado para evitar duplicados
    boolean existsByNumero(String numero);
}
