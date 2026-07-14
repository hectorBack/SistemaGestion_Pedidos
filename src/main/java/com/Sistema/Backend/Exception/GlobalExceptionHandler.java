package com.Sistema.Backend.Exception;

import com.Sistema.Backend.Config.ErrorRespuestaDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.Sistema.Backend.Controller")
public class GlobalExceptionHandler {

    // 1. Captura errores de "No encontrado" (404)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 2. Captura errores de validación de los DTOs (400)
    // Ejemplo: cuando el WhatsApp no tiene 4 dígitos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarValidaciones(MethodArgumentNotValidException ex) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorRespuestaDTO error = new ErrorRespuestaDTO(
                LocalDateTime.now(),
                "Error de validación en los datos",
                errores
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 3. Captura cualquier otro error inesperado (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarGlobal(Exception ex, WebRequest request) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(
                LocalDateTime.now(),
                "Ocurrió un error interno en el servidor",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarNotFound(ResourceNotFoundException ex, WebRequest request) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 400 Bad Request (Para lógica de negocio fallida)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarNegocio(BusinessException ex, WebRequest request) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(LocalDateTime.now(), "Error de operación", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarUsuarioDeshabilitado(DisabledException ex, WebRequest request) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(
                LocalDateTime.now(),
                "CUENTA_DESHABILITADA", // Este código es el que leerá tu frontend en Vue
                "Tu cuenta se encuentra temporalmente deshabilitada. Contacta al administrador."
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN); // Retorna 403 Forbidden
    }

    // 5. Captura explícita de credenciales incorrectas (401)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorRespuestaDTO> manejarCredencialesIncorrectas(BadCredentialsException ex, WebRequest request) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(
                LocalDateTime.now(),
                "CREDANCIALES_INVALIDAS",
                "El nombre de usuario o la contraseña son incorrectos."
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED); // Retorna 401 Unauthorized
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> manejarRuntimeException(RuntimeException ex) {
        Map<String, String> respuesta = new HashMap<>();

        // Aquí capturamos el mensaje exacto: "El correo electrónico ya está registrado"
        respuesta.put("message", ex.getMessage());

        // Retornamos un 400 Bad Request en lugar de un Error 500
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }
}
