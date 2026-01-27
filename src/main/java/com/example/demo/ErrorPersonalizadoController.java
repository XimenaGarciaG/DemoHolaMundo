package com.example.demo;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorPersonalizadoController implements ErrorController {

    @RequestMapping("/error")
    public String manejarError(HttpServletRequest request, Model model) {
        // Obtener el código de error
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        // Obtener detalles del error
        Object mensajeError = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        // Configurar valores por defecto
        int codigoError = 500;
        String titulo = "Error del Servidor";
        String descripcion = "Ha ocurrido un error inesperado.";
        String icono = "bi-exclamation-octagon";
        
        if (status != null) {
            codigoError = Integer.parseInt(status.toString());
            
            switch (codigoError) {
                case 400:
                    titulo = "Solicitud Incorrecta";
                    descripcion = "La solicitud no se puede procesar.";
                    icono = "bi-exclamation-triangle";
                    break;
                case 401:
                    titulo = "No Autorizado";
                    descripcion = "No tienes permiso para acceder a esta página.";
                    icono = "bi-shield-exclamation";
                    break;
                case 403:
                    titulo = "Prohibido";
                    descripcion = "No tienes acceso a este recurso.";
                    icono = "bi-ban";
                    break;
                case 404:
                    titulo = "Página No Encontrada";
                    descripcion = "La página que buscas no existe o ha sido movida.";
                    icono = "bi-question-octagon";
                    break;
                case 500:
                    titulo = "Error del Servidor";
                    descripcion = "Ha ocurrido un error interno en el servidor.";
                    icono = "bi-exclamation-octagon";
                    break;
                case 503:
                    titulo = "Servicio No Disponible";
                    descripcion = "El servicio no está disponible temporalmente.";
                    icono = "bi-wifi-off";
                    break;
                default:
                    titulo = "Error " + codigoError;
                    descripcion = "Ha ocurrido un error.";
                    icono = "bi-exclamation-circle";
            }
        }
        
        // Agregar datos al modelo
        model.addAttribute("codigoError", codigoError);
        model.addAttribute("tituloError", titulo);
        model.addAttribute("descripcionError", descripcion);
        model.addAttribute("iconoError", icono);
        model.addAttribute("mensajeError", mensajeError != null ? mensajeError.toString() : "");
        model.addAttribute("exception", exception != null ? exception.toString() : "");
        model.addAttribute("uri", uri != null ? uri.toString() : "");
        
        return "error-personalizado";
    }
}