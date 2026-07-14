package com.Sistema.Backend.Clientes.Repository;

import com.Sistema.Backend.Clientes.Entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar por el ID de la cuenta de usuario vinculada
    Optional<Cliente> findByUsuarioId(Long usuarioId);

    // Consulta avanzada con filtros y paginación en tiempo real para el panel de administración
    @Query("SELECT c FROM Cliente c JOIN c.usuario u WHERE " +
            "(:nombre IS NULL OR LOWER(c.nombreCompleto) LIKE LOWER(CONCAT('%', CAST(:nombre AS string), '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:nombre AS string), '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:nombre AS string), '%'))) " +
            "AND (:activo IS NULL OR c.activo = :activo)")
    Page<Cliente> buscarClientesPaginados(
            @Param("nombre") String nombre,
            @Param("activo") Boolean activo,
            Pageable pageable
    );
}
