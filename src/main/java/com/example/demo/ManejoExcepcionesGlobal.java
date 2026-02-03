package com.example.demo;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import jakarta.servlet.http.HttpServletRequest;
import com.example.demo.exceptions.ResourceNotFoundException;
import java.util.Date;

@ControllerAdvice
public class ManejoExcepcionesGlobal {
    
    // Excepción personalizada para 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public String manejarRecursoNoEncontrado(ResourceNotFoundException ex, Model model, WebRequest request) {
        return configurarError(ex, model, "Recurso no encontrado: " + ex.getMessage(), 
                               404, "No encontrado", request);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public String manejarArgumentoInvalido(IllegalArgumentException ex, Model model, WebRequest request) {
        return configurarError(ex, model, "Parámetro inválido: " + ex.getMessage(), 
                               400, "Bad Request", request);
    }
    
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public String manejarErrorBD(org.springframework.dao.DataAccessException ex, Model model, WebRequest request) {
        return configurarError(ex, model, "Error en la base de datos: " + ex.getMessage(), 
                               503, "Service Unavailable", request);
    }
    
    // Capturar excepciones 404 de Spring
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public String manejarNoHandlerFound(org.springframework.web.servlet.NoHandlerFoundException ex, 
                                        Model model, WebRequest request) {
        return configurarError(ex, model, "Página no encontrada: " + ex.getRequestURL(), 
                               404, "Página no encontrada", request);
    }
    
    // Capturar TODAS las excepciones
    @ExceptionHandler(Exception.class)
    public String manejarExcepcionGeneral(Exception ex, Model model, WebRequest request, 
                                          HttpServletRequest httpRequest) {
        return configurarError(ex, model, "Error interno: " + ex.getMessage(), 
                               500, "Error interno del servidor", request);
    }
    
    // Método auxiliar para configurar errores
    private String configurarError(Exception ex, Model model, String mensaje, 
                                   int codigo, String tipo, WebRequest request) {
        
        model.addAttribute("error", mensaje);
        model.addAttribute("detalles", ex.toString());
        model.addAttribute("timestamp", new Date());
        model.addAttribute("codigoError", codigo);
        model.addAttribute("tipoError", tipo);
        model.addAttribute("uri", request.getDescription(false));
        
        // Log para debugging
        System.err.println("=== ERROR " + codigo + " ===");
        System.err.println("Tipo: " + ex.getClass().getName());
        System.err.println("Mensaje: " + ex.getMessage());
        
        return "error-personalizado";
    }
}