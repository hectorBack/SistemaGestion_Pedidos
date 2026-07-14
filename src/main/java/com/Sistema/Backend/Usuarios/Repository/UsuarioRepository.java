package com.Sistema.Backend.Usuarios.Repository;

import com.Sistema.Backend.Usuarios.Entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    // Opcional por si en el futuro decides permitir login con Email
    Optional<Usuario> findByEmail(String email);

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    // QUERY NATIVA AL ESTILO DE CATEGORÍAS
    @Query(value = "SELECT * FROM usuarios u WHERE " +
            "(:username IS NULL OR u.username ILIKE CONCAT('%', CAST(:username AS VARCHAR), '%')) AND " +
            "(CAST(:activo AS BOOLEAN) IS NULL OR u.activo = CAST(:activo AS BOOLEAN)) " +
            "ORDER BY u.id DESC", // Puedes cambiar el ordenamiento por el campo que prefieras
            countQuery = "SELECT count(*) FROM usuarios u WHERE " +
                    "(:username IS NULL OR u.username ILIKE CONCAT('%', CAST(:username AS VARCHAR), '%')) AND " +
                    "(CAST(:activo AS BOOLEAN) IS NULL OR u.activo = CAST(:activo AS BOOLEAN))",
            nativeQuery = true)
    Page<Usuario> buscarTodosParaAdminPaginado(
            @Param("username") String username,
            @Param("activo") Boolean activo,
            Pageable pageable);
}