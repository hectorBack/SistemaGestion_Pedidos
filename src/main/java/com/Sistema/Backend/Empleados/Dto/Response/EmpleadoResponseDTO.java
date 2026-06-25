package com.Sistema.Backend.Empleados.Dto.Response;

import com.Sistema.Backend.Empleados.Entity.PuestoEmpleado;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpleadoResponseDTO {

    private Long id;
    private String nombre;
    private PuestoEmpleado puesto;
    private boolean activo;
}
