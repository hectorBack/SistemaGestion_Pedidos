package com.Sistema.Backend.Mesas.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum EstadoMesa
{
    LIBRE,      // Disponible para recibir clientes
    OCUPADA,    // Tiene un pedido activo vinculando comensales
    RESERVADA,  // Bloqueada para una hora específica
    SUCIA       // Requiere limpieza antes de volver a estar LIBRE
}
