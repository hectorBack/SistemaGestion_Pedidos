package com.Sistema.Backend.Empleados.Dto.Request;

import com.Sistema.Backend.Empleados.Entity.PuestoEmpleado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpleadoRequestDTO {

    @NotBlank(message = "El nombre del empleado no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotNull(message = "El puesto del empleado es mandatorio")
    private PuestoEmpleado puesto;
}
