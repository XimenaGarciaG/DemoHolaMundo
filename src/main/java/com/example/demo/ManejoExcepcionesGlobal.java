package com.example.demo;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

@ControllerAdvice
public class ManejoExcepcionesGlobal {
    
    @ExceptionHandler(Exception.class)
    public String manejarTodasExcepciones(Exception ex, Model model, HttpServletRequest request) {
        
        // Determinar código de error
        int codigoError = 500;
        String tipoError = "Error del Servidor";
        
        String className = ex.getClass().getSimpleName();
        if (className.contains("NotFound")) {
            codigoError = 404;
            tipoError = "No Encontrado";
        } else if (className.contains("IllegalArgument") || className.contains("BadRequest")) {
            codigoError = 400;
            tipoError = "Solicitud Incorrecta";
        }
        
        // Pasar variables ESSENCIALES con valores por defecto
        model.addAttribute("codigoError", codigoError);
        model.addAttribute("tipoError", tipoError);
        model.addAttribute("error", ex.getMessage() != null ? ex.getMessage() : "Ha ocurrido un error inesperado.");
        model.addAttribute("uri", request.getRequestURI());
        model.addAttribute("timestamp", new Date());
        
        return "error-personalizado";
    }
}