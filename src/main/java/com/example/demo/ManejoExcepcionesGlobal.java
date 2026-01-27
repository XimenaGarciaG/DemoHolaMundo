package com.example.demo;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import com.example.demo.exceptions.ResourceNotFoundException;
import java.util.Date;

@ControllerAdvice
public class ManejoExcepcionesGlobal {
    
    // 1. Primero las excepciones MÁS ESPECÍFICAS
    @ExceptionHandler(ResourceNotFoundException.class)
    public String manejarRecursoNoEncontrado(ResourceNotFoundException ex, Model model, WebRequest request) {
        model.addAttribute("error", "Recurso no encontrado: " + ex.getMessage());
        model.addAttribute("detalles", ex.toString());
        model.addAttribute("timestamp", new Date());
        model.addAttribute("codigoError", 404);
        model.addAttribute("tipoError", "Recurso no encontrado");
        return "error-personalizado";
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public String manejarArgumentoInvalido(IllegalArgumentException ex, Model model, WebRequest request) {
        model.addAttribute("error", "Parámetro inválido: " + ex.getMessage());
        model.addAttribute("detalles", ex.toString());
        model.addAttribute("timestamp", new Date());
        model.addAttribute("codigoError", 400);
        model.addAttribute("tipoError", "Error de validación");
        return "error-personalizado";
    }
    
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public String manejarErrorBD(org.springframework.dao.DataAccessException ex, Model model, WebRequest request) {
        model.addAttribute("error", "Error en la base de datos: " + ex.getMessage());
        model.addAttribute("detalles", ex.toString());
        model.addAttribute("timestamp", new Date());
        model.addAttribute("codigoError", 503);
        model.addAttribute("tipoError", "Error de base de datos");
        return "error-personalizado";
    }
    
    // 2. Excepciones de Spring MVC
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public String manejarPaginaNoEncontrada(Exception ex, Model model, WebRequest request) {
        model.addAttribute("error", "Página no encontrada: " + request.getDescription(false));
        model.addAttribute("detalles", ex.toString());
        model.addAttribute("timestamp", new Date());
        model.addAttribute("codigoError", 404);
        model.addAttribute("tipoError", "Página no encontrada");
        return "error-personalizado";
    }
    
    // 3. ÚLTIMO: La excepción más general (Exception.class)
    @ExceptionHandler(Exception.class)
    public String manejarExcepcionGeneral(Exception ex, Model model, WebRequest request) {
        model.addAttribute("error", "Ha ocurrido un error inesperado: " + ex.getMessage());
        model.addAttribute("detalles", ex.toString());
        model.addAttribute("timestamp", new Date());
        model.addAttribute("codigoError", 500);
        model.addAttribute("tipoError", "Error interno del servidor");
        return "error-personalizado";
    }
}