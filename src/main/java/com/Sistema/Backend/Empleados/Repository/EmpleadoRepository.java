package com.Sistema.Backend.Empleados.Repository;

import com.Sistema.Backend.Empleados.Entity.Empleado;
import com.Sistema.Backend.Empleados.Entity.PuestoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    // Método clave para llenar el <select> de tu modal de apertura de mesas
    List<Empleado> findByPuestoAndActivoTrue(PuestoEmpleado puesto);

    // Listar todos los activos en general
    List<Empleado> findByActivoTrue();
}
