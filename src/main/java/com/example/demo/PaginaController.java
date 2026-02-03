package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PaginaController {
    
    // Lista en memoria para el formulario principal
    private List<Persona> personasRegistradas = new ArrayList<>();
    private int contadorId = 1;
    
    // Repositorio para la tabla REGISTROS en H2
    @Autowired
    private RegistroRepository registroRepository;
    
    // Datos del desarrollador
    private final Desarrollador desarrollador = new Desarrollador(
        "Ximena Garcia", 
        "ximena.email@example.com",
        25,
        "1234567890",
        "Estudiante de Ingeniera en Desarrollo y Gestion de Software",
        "Spring Boot, Java, HTML, CSS, JavaScript",
        "https://github.com/tuusuario",
        "Aplicación web con Spring Boot MVC y H2 Database"
    );
    // ===== PANTALLA DE INICIO (ACTUALIZADO) =====
    @GetMapping("/")
    public String mostrarInicio(@RequestParam(value = "exito", required = false) String exito,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "nombre", required = false) String nombre,
                            @RequestParam(value = "total", required = false) Long total,
                            Model model) {
        
        model.addAttribute("mensaje", "¡Hola Mundo desde Spring Boot!");
        model.addAttribute("personas", personasRegistradas);
        
        // Contar registros en H2
        long totalRegistrosH2 = registroRepository.count();
        model.addAttribute("totalRegistrosH2", totalRegistrosH2);
        
        // Manejar mensajes de éxito/error
        if ("true".equals(exito) && nombre != null && total != null) {
            model.addAttribute("exito", "Registro '" + nombre + "' insertado en H2. Total: " + total);
        } else if ("nombre_vacio".equals(error)) {
            model.addAttribute("error", "El nombre no puede estar vacío");
        } else if ("true".equals(error)) {
            model.addAttribute("error", "Ocurrió un error al insertar el registro");
        }
        
        return "inicio";
    }

   // ===== PROCESAR FORMULARIO PARA INSERTAR EN H2 CON MANEJO DE ERRORES =====
    @PostMapping("/insertar-h2")
    public String insertarEnH2(@RequestParam("nombre") String nombre, Model model) {
        try {
            // Validación
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            
            String nombreLimpio = nombre.trim();
            
            // Validar longitud máxima
            if (nombreLimpio.length() > 100) {
                throw new IllegalArgumentException("El nombre no puede tener más de 100 caracteres");
            }
            
            // Crear registro
            Registro registro = new Registro();
            registro.setNombre(nombreLimpio);
            
            // Intentar guardar
            registroRepository.save(registro);
            
            // Obtener total
            long total = registroRepository.count();
            
            // Éxito
            model.addAttribute("mensaje", "¡Hola Mundo desde Spring Boot!");
            model.addAttribute("exito", "Registro '" + nombreLimpio + "' insertado en H2. Total: " + total);
            model.addAttribute("totalRegistrosH2", total);
            
            return "inicio";
            
        } catch (IllegalArgumentException e) {
            // Error de validación
            model.addAttribute("error", "Error de validación: " + e.getMessage());
            model.addAttribute("mensaje", "¡Hola Mundo desde Spring Boot!");
            model.addAttribute("totalRegistrosH2", registroRepository.count());
            return "inicio";
            
        } catch (Exception e) {
            // Error grave
            throw new RuntimeException("Error al insertar en base de datos: " + e.getMessage(), e);
        }
    }
    
    // ===== VER REGISTROS DE H2 =====
   @GetMapping("/ver-registros-h2")
    public String verRegistrosH2(Model model) {
        // Obtener todos los registros de la base de datos
        List<Registro> registros = registroRepository.findAll(); // Esto devuelve lista vacía si no hay datos
        
        if (registros.isEmpty()) {
        throw new ResourceNotFoundException("No hay registros");
        }

        model.addAttribute("registros", registros);
        model.addAttribute("titulo", "Registros en Base de Datos H2");
        
        // NO lances excepción si está vacío - es normal
        return "var-registros-h2";
    }
    
    // ===== ELIMINAR TODOS LOS REGISTROS H2 =====
    @PostMapping("/limpiar-registros-h2")
    public String limpiarRegistrosH2() {
        try {
            registroRepository.deleteAll();
            return "redirect:/ver-registros-h2?exito=Todos los registros han sido eliminados";
        } catch (Exception e) {
            return "redirect:/ver-registros-h2?error=Error al eliminar registros";
        }
    }
    
    // ===== PANTALLA PRINCIPAL CON TABLA (lista en memoria) =====
    @GetMapping("/principal")
    public String mostrarPrincipal(Model model) {
        model.addAttribute("personas", personasRegistradas);
        model.addAttribute("titulo", "Gestión de Personas (Memoria)");
        model.addAttribute("totalPersonas", personasRegistradas.size());
        return "persona-principal";
    }
    
    // ===== PANTALLA "ACERCA DE" =====
    @GetMapping("/acerca")
    public String mostrarAcercaDe(Model model) {
        model.addAttribute("desarrollador", desarrollador);
        return "acerca-de";
    }
    
    // ===== PANTALLA DE REGISTRO (GET) =====
    @GetMapping("/registrar")
    public String mostrarFormulario(Model model) {
        return "registrar-persona";
    }
    
    // ===== PROCESAR REGISTRO (POST) - Solo memoria =====
    @PostMapping("/registrar")
    public String procesarRegistro(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam int edad,
            @RequestParam String telefono,
            @RequestParam(required = false) String recaptchaResponse,
            Model model) {
        
        // Validar reCAPTCHA
        if (recaptchaResponse == null || !recaptchaResponse.equals("verified")) {
            model.addAttribute("error", "Por favor, verifica que no eres un robot");
            return "registrar-persona";
        }
        
        // Validaciones básicas
        if (nombre == null || nombre.trim().isEmpty()) {
            model.addAttribute("error", "El nombre es obligatorio");
            return "registrar-persona";
        }
        
        if (edad < 18) {
            model.addAttribute("error", "Debes ser mayor de 18 años");
            return "registrar-persona";
        }
        
        // Crear nueva persona (solo en memoria)
        Persona persona = new Persona();
        persona.setId(contadorId++);
        persona.setNombre(nombre);
        persona.setEmail(email);
        persona.setEdad(edad);
        persona.setTelefono(telefono);
        
        // Agregar a la lista en memoria (NO a la BD)
        personasRegistradas.add(persona);
        
        return "redirect:/principal?exito=Persona registrada en memoria: " + nombre;
    }
    
    // ===== ELIMINAR REGISTRO (solo memoria) =====
    @PostMapping("/eliminar/{id}")
    public String eliminarRegistro(@PathVariable int id) {
        personasRegistradas.removeIf(p -> p.getId() == id);
        return "redirect:/principal?exito=Registro eliminado de memoria";
    }

    // En tu PaginaController.java, agrega estos métodos:

    // ===== SIMULAR ERROR 404 =====
    @GetMapping("/error-404")
    public String simularError404() {
        throw new ResourceNotFoundException("Recurso no encontrado");
    }

    // ===== SIMULAR ERROR 500 =====
    @GetMapping("/error-500")
    public String simularError500() {
        throw new RuntimeException("Error interno del servidor");
    }

    // ===== SIMULAR ERROR DE VALIDACIÓN =====
    @GetMapping("/error-validacion")
    public String simularErrorValidacion() {
        throw new IllegalArgumentException("Parámetros inválidos");
    }

    // ===== SIMULAR ERROR DE BASE DE DATOS =====
    @GetMapping("/error-db")
    public String simularErrorDB() {
        throw new DataAccessException("Error de conexión a base de datos") {};
    }

    // ===== EXCEPCIÓN PERSONALIZADA =====
    public class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    // En PaginaController.java
    @GetMapping("/probar-error/{tipo}")
    public String probarError(@PathVariable String tipo) {
        switch (tipo.toLowerCase()) {
            case "404":
                throw new ResourceNotFoundException("Este es un error 404 de prueba");
            case "400":
                throw new IllegalArgumentException("Parámetro inválido de prueba");
            case "500":
                throw new RuntimeException("Error interno de prueba");
            case "db":
                throw new org.springframework.dao.DataAccessException("Error de BD de prueba") {};
            default:
                return "redirect:/";
        }
    }
        
    // ===== CLASE PERSONA (solo memoria) =====
    public static class Persona {
        private int id;
        private String nombre;
        private String email;
        private int edad;
        private String telefono;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public int getEdad() { return edad; }
        public void setEdad(int edad) { this.edad = edad; }
        
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
    }
    
    // ===== CLASE DESARROLLADOR =====
    public static class Desarrollador {
        private String nombre;
        private String email;
        private int edad;
        private String telefono;
        private String profesion;
        private String habilidades;
        private String github;
        private String descripcionProyecto;
        
        public Desarrollador(String nombre, String email, int edad, String telefono, 
                           String profesion, String habilidades, String github, 
                           String descripcionProyecto) {
            this.nombre = nombre;
            this.email = email;
            this.edad = edad;
            this.telefono = telefono;
            this.profesion = profesion;
            this.habilidades = habilidades;
            this.github = github;
            this.descripcionProyecto = descripcionProyecto;
        }
        
        public String getNombre() { return nombre; }
        public String getEmail() { return email; }
        public int getEdad() { return edad; }
        public String getTelefono() { return telefono; }
        public String getProfesion() { return profesion; }
        public String getHabilidades() { return habilidades; }
        public String getGithub() { return github; }
        public String getDescripcionProyecto() { return descripcionProyecto; }
    }
}