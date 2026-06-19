package com.Sistema.Backend.Categorias.Repository;

import com.Sistema.Backend.Categorias.Entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);

    // Spring Data genera la query automáticamente basándose en el nombre del método
    List<Categoria> findByActivoTrueOrderByOrdenAsc();

    @Query(value = "SELECT * FROM categorias WHERE LOWER(nombre) = LOWER(:nombre)", nativeQuery = true)
    Optional<Categoria> encontrarTodasPorNombreNativo(@Param("nombre") String nombre);

    @Query(value = "SELECT * FROM categorias WHERE id = :id", nativeQuery = true)
    Optional<Categoria> encontrarPorIdNativo(@Param("id") Long id);

    // QUERY NATIVA PAGINADA: Se salta el @SQLRestriction para que el administrador pueda listar todo el historial
    @Query(value = "SELECT * FROM categorias c WHERE " +
            "(:nombre IS NULL OR c.nombre ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%')) AND " +
            "(CAST(:activo AS BOOLEAN) IS NULL OR c.activo = CAST(:activo AS BOOLEAN))" +
            "ORDER BY c.orden ASC",
            countQuery = "SELECT count(*) FROM categorias c WHERE " +
                    "(:nombre IS NULL OR c.nombre ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%')) AND " +
                    "(CAST(:activo AS BOOLEAN) IS NULL OR c.activo = CAST(:activo AS BOOLEAN))",
            nativeQuery = true)
    Page<Categoria> buscarTodasParaAdminPaginado(
            @Param("nombre") String nombre,
            @Param("activo") Boolean activo,
            Pageable pageable);
}


