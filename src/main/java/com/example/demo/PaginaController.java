package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import com.example.demo.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PaginaController {
    
    // Lista en memoria para el formulario principal
    private List<Persona> personasRegistradas = new ArrayList<>();
    private int contadorId = 1;
    
    // Repositorios
    @Autowired
    private RegistroRepository registroRepository;
    @Autowired
    private ImagenCarruselRepository imagenCarruselRepository;
    @Autowired
    private PersonaRepository personaRepository;
    
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
    // ===== ÚNICO MÉTODO PARA LA PÁGINA DE INICIO =====
    @GetMapping("/")
    public String mostrarInicio(
            @RequestParam(value = "exito", required = false) String exito,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "total", required = false) Long total,
            Model model) {
        
        // Datos base
        model.addAttribute("mensaje", "¡Hola Mundo desde Spring Boot!");
        model.addAttribute("personas", personasRegistradas);
        
        // CONTAR REGISTROS EN POSTGRESQL (ya no es H2)
        long totalRegistros = registroRepository.count();
        model.addAttribute("totalRegistrosPG", totalRegistros);  // Cambié el nombre
        
        // Obtener imágenes para carrusel
        List<ImagenCarrusel> imagenesCarrusel = imagenCarruselRepository.findByActivaTrueOrderByOrdenAsc();
        
        // Limitar a 5 imágenes
        if (imagenesCarrusel.size() > 5) {
            imagenesCarrusel = imagenesCarrusel.subList(0, 5);
        }
        
        model.addAttribute("imagenesCarrusel", imagenesCarrusel);
        model.addAttribute("totalImagenes", imagenCarruselRepository.count());
        
        // Manejar mensajes de éxito/error
        if (exito != null) {
            if (nombre != null && total != null) {
                model.addAttribute("exito", "✓ Registro '" + nombre + "' insertado en PostgreSQL. Total: " + total);
            } else {
                model.addAttribute("exito", exito);
            }
        }
        
        if (error != null) {
            if ("nombre_vacio".equals(error)) {
                model.addAttribute("error", "✗ El nombre no puede estar vacío");
            } else {
                model.addAttribute("error", error);
            }
        }
        
        return "inicio";
    }

   // ===== PROCESAR FORMULARIO PARA INSERTAR EN H2 CON MANEJO DE ERRORES =====
    @PostMapping("/insertar-h2")
    public String insertarEnPostgres(@RequestParam("nombre") String nombre, RedirectAttributes redirectAttributes) {
        try {
            // Validación
            if (nombre == null || nombre.trim().isEmpty()) {
                redirectAttributes.addAttribute("error", "nombre_vacio");
                return "redirect:/";
            }
            
            String nombreLimpio = nombre.trim();
            
            // Validar longitud máxima
            if (nombreLimpio.length() > 100) {
                redirectAttributes.addAttribute("error", "El nombre no puede tener más de 100 caracteres");
                return "redirect:/";
            }
            
            // Crear registro
            Registro registro = new Registro();
            registro.setNombre(nombreLimpio);
            
            // Intentar guardar
            registroRepository.save(registro);
            
            // Obtener total
            long total = registroRepository.count();
            
            redirectAttributes.addAttribute("exito", "true");
            redirectAttributes.addAttribute("nombre", nombreLimpio);
            redirectAttributes.addAttribute("total", total);
            
            return "redirect:/";
            
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error al insertar: " + e.getMessage());
            return "redirect:/";
        }
    }
    
   // ===== VER REGISTROS EN POSTGRESQL =====
    @GetMapping("/ver-registros")
    public String verRegistros(Model model) {
        List<Registro> registros = registroRepository.findAll();
        model.addAttribute("registros", registros);
        model.addAttribute("titulo", "Registros en PostgreSQL");
        model.addAttribute("totalRegistros", registros.size());
        return "ver-registros";  // Cambia el nombre del HTML si es necesario
    }
    
    // ===== ELIMINAR TODOS LOS REGISTROS =====
    @PostMapping("/limpiar-registros")
    public String limpiarRegistros() {
        try {
            registroRepository.deleteAll();
            return "redirect:/ver-registros?exito=Todos los registros han sido eliminados";
        } catch (Exception e) {
            return "redirect:/ver-registros?error=Error al eliminar registros";
        }
    }
    
    // ===== PANTALLA PRINCIPAL CON PAGINACIÓN =====
    @GetMapping("/principal")
    public String mostrarPrincipal(
            @RequestParam(required = false, defaultValue = "0") int pagina,
            @RequestParam(required = false, defaultValue = "5") int tamano,
            Model model) {
        
        try {
            System.out.println("=== CARGANDO PÁGINA PRINCIPAL ===");
            
            // Validar parámetros
            if (pagina < 0) pagina = 0;
            if (tamano <= 0 || tamano > 100) tamano = 5;
            
            // OBTENER TODOS LOS REGISTROS PRIMERO (para debug)
            List<PersonaEntity> todas = personaRepository.findAll();
            System.out.println("TOTAL REGISTROS EN BD: " + todas.size());
            
            // Si hay registros, mostrarlos en consola
            if (!todas.isEmpty()) {
                System.out.println("PRIMER REGISTRO: " + todas.get(0).getNombre());
            }
            
            // Crear pageable
            Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("fechaRegistro").descending());
            Page<PersonaEntity> pagePersonas = personaRepository.findAll(pageable);
            
            System.out.println("PÁGINA ACTUAL - Registros: " + pagePersonas.getNumberOfElements());
            System.out.println("TOTAL PÁGINAS: " + pagePersonas.getTotalPages());
            
            // Agregar AL MODELO (VERIFICAR QUE ESTO SÍ SE HACE)
            model.addAttribute("personasBD", pagePersonas.getContent());
            model.addAttribute("totalPersonasBD", pagePersonas.getTotalElements());
            model.addAttribute("totalPaginas", pagePersonas.getTotalPages());
            model.addAttribute("paginaActual", pagina);
            model.addAttribute("tamanoPagina", tamano);
            model.addAttribute("tieneAnterior", pagePersonas.hasPrevious());
            model.addAttribute("tieneSiguiente", pagePersonas.hasNext());
            
            if (pagePersonas.hasPrevious()) {
                model.addAttribute("paginaAnterior", pagePersonas.previousPageable().getPageNumber());
            }
            if (pagePersonas.hasNext()) {
                model.addAttribute("paginaSiguiente", pagePersonas.nextPageable().getPageNumber());
            }
            
            int[] tamanosOpciones = {5, 10, 20, 50};
            model.addAttribute("tamanosOpciones", tamanosOpciones);
            
            // DEBUG - Imprimir qué hay en el modelo
            System.out.println("model.getAttribute('personasBD'): " + model.getAttribute("personasBD"));
            System.out.println("model.getAttribute('totalPersonasBD'): " + model.getAttribute("totalPersonasBD"));
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("personasBD", new ArrayList<>());
            model.addAttribute("totalPersonasBD", 0);
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        
        return "persona-principal";
    }

    // TEMPORAL
    @GetMapping("/debug-personas")
    @ResponseBody
    public String debugPersonas() {
        try {
            List<PersonaEntity> todas = personaRepository.findAll();
            StringBuilder sb = new StringBuilder();
            
            sb.append("📊 TOTAL EN BD: ").append(todas.size()).append("\n\n");
            
            if (todas.isEmpty()) {
                sb.append("❌ NO HAY REGISTROS EN LA BASE DE DATOS\n");
                sb.append("Verifica que:\n");
                sb.append("1. La tabla 'personas' existe en PostgreSQL\n");
                sb.append("2. Los registros se están guardando correctamente\n");
                sb.append("3. La conexión a BD es correcta\n");
            } else {
                sb.append("✅ REGISTROS ENCONTRADOS:\n");
                sb.append("=======================\n");
                for (PersonaEntity p : todas) {
                    sb.append(String.format("ID: %d | Nombre: %-20s | Email: %-25s | Edad: %d | Fecha: %s\n",
                        p.getId(), 
                        p.getNombre(), 
                        p.getEmail(), 
                        p.getEdad(),
                        p.getFechaRegistro().toString()));
                }
            }
            
            return sb.toString().replace("\n", "<br>");
        } catch (Exception e) {
            return "❌ ERROR: " + e.getMessage() + "<br>" + e.toString().replace("\n", "<br>");
        }
    }
    //-------------------------------------------------------------------------------

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
    
    // ===== PROCESAR REGISTRO (POST) - AHORA GUARDA EN POSTGRESQL =====
    @PostMapping("/registrar")
    public String procesarRegistro(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam int edad,
            @RequestParam(required = false) String telefono,
            @RequestParam String estadoCivil,  // NUEVO
            @RequestParam(required = false) String recaptchaResponse,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validar reCAPTCHA
            if (recaptchaResponse == null || !recaptchaResponse.equals("verified")) {
                redirectAttributes.addFlashAttribute("error", "Por favor, verifica que no eres un robot");
                return "redirect:/registrar";
            }
            
            // Validaciones básicas
            if (nombre == null || nombre.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El nombre es obligatorio");
                return "redirect:/registrar";
            }
            
            if (edad < 18) {
                redirectAttributes.addFlashAttribute("error", "Debes ser mayor de 18 años");
                return "redirect:/registrar";
            }
            
            // Validar email único
            if (personaRepository.findByEmail(email).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
                return "redirect:/registrar";
            }

            // Validar estado civil
            if (estadoCivil == null || estadoCivil.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debes seleccionar un estado civil");
                return "redirect:/registrar";
            }
            
            // Crear nueva persona en PostgreSQL
            PersonaEntity persona = new PersonaEntity();
            persona.setNombre(nombre.trim());
            persona.setEmail(email.trim());
            persona.setEdad(edad);
            persona.setTelefono(telefono != null ? telefono.trim() : "");
            persona.setEstadoCivil(estadoCivil);  // NUEVO
            persona.setPassword("default123"); // Contraseña por defecto (deberías encriptarla)
            persona.setAceptaTerminos(true);
            
            // Guardar en PostgreSQL
            personaRepository.save(persona);
            
            // También mantener en memoria si quieres (opcional)
            Persona personaMemoria = new Persona();
            personaMemoria.setId(contadorId++);
            personaMemoria.setNombre(nombre);
            personaMemoria.setEmail(email);
            personaMemoria.setEdad(edad);
            personaMemoria.setTelefono(telefono);
            personasRegistradas.add(personaMemoria);
            
            redirectAttributes.addFlashAttribute("exito", 
                "✓ Persona registrada exitosamente en PostgreSQL. ID: " + persona.getId());
            
            return "redirect:/principal";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "✗ Error al registrar: " + e.getMessage());
            return "redirect:/registrar";
        }
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

    @GetMapping("/ver-error")
    public String verErrorDirecto(Model model, HttpServletRequest request) {
        // Pasar datos directamente a la plantilla (sin excepción)
        model.addAttribute("codigoError", 404);
        model.addAttribute("tipoError", "Página No Encontrada");
        model.addAttribute("error", "La página que buscas no existe o ha sido movida.");
        model.addAttribute("detalles", "Endpoint de prueba - Recurso no encontrado");
        model.addAttribute("uri", request.getRequestURI());
        
        return "error-personalizado";
    }
    
    // ===== ENDPOINT QUE GENERA ERROR INTENCIONAL =====
    
    @GetMapping("/generar-error/{tipo}")
    public String generarError(@PathVariable String tipo, Model model) {
        // Este método NO lanza excepción, solo muestra la página
        int codigo;
        String mensaje;
        
        switch (tipo.toLowerCase()) {
            case "notfound":
                codigo = 404;
                mensaje = "Recurso no encontrado";
                break;
            case "badrequest":
                codigo = 400;
                mensaje = "Solicitud incorrecta";
                break;
            case "server":
                codigo = 500;
                mensaje = "Error interno del servidor";
                break;
            default:
                codigo = 500;
                mensaje = "Error desconocido";
        }
        
        model.addAttribute("codigoError", codigo);
        model.addAttribute("tipoError", getTipoErrorPorCodigo(codigo));
        model.addAttribute("error", mensaje + " (Simulado)");
        model.addAttribute("detalles", "Error simulado para pruebas - Código: " + codigo);
        
        return "error-personalizado";
    }
    
    private String getTipoErrorPorCodigo(int codigo) {
        switch (codigo) {
            case 404: return "No Encontrado";
            case 400: return "Solicitud Incorrecta";
            case 500: return "Error del Servidor";
            default: return "Error";
        }
    }
    
    // ===== EDITAR PERSONA (GET) =====
    @GetMapping("/editar/{id}")
    public String editarPersona(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            PersonaEntity persona = personaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con ID: " + id));
            
            model.addAttribute("persona", persona);
            return "editar-persona";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar la persona: " + e.getMessage());
            return "redirect:/principal";
        }
    }

    // ===== ACTUALIZAR PERSONA (POST) =====
    @PostMapping("/actualizar/{id}")
    public String actualizarPersona(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam int edad,
            @RequestParam(required = false) String telefono,
            @RequestParam String estadoCivil,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validaciones básicas
            if (nombre == null || nombre.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El nombre es obligatorio");
                return "redirect:/editar/" + id;
            }
            
            if (edad < 18) {
                redirectAttributes.addFlashAttribute("error", "Debes ser mayor de 18 años");
                return "redirect:/editar/" + id;
            }
            
            // Buscar la persona existente
            PersonaEntity persona = personaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada"));
            
            // Verificar si el email ya está siendo usado por OTRA persona
            if (!persona.getEmail().equals(email)) {
                if (personaRepository.findByEmail(email).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "El email ya está registrado por otra persona");
                    return "redirect:/editar/" + id;
                }
            }
            
            // Actualizar datos
            persona.setNombre(nombre.trim());
            persona.setEmail(email.trim());
            persona.setEdad(edad);
            persona.setTelefono(telefono != null ? telefono.trim() : "");
            persona.setEstadoCivil(estadoCivil);
            
            // Guardar cambios
            personaRepository.save(persona);
            
            redirectAttributes.addFlashAttribute("exito", 
                "✓ Persona actualizada exitosamente. ID: " + persona.getId());
            
            return "redirect:/principal";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/editar/" + id;
        }
    }

    // ===== ELIMINAR PERSONA =====
    @PostMapping("/eliminar/{id}")
    public String eliminarPersona(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            PersonaEntity persona = personaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada"));
            
            personaRepository.deleteById(id);
            
            redirectAttributes.addFlashAttribute("exito", 
                "✓ Persona eliminada exitosamente: " + persona.getNombre());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        
        return "redirect:/principal";
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