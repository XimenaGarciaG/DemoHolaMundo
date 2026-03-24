package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.service.ImgBBService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/carrusel")
public class CarruselController {
    
    @Autowired
    private ImagenCarruselRepository imagenCarruselRepository;
    
    @Autowired
    private ImgBBService imgBBService;
    
    // ===== VER CARRUSEL =====
    @GetMapping
    public String verCarrusel(Model model) {
        List<ImagenCarrusel> imagenes = imagenCarruselRepository.findByActivaTrueOrderByOrdenAsc();
        model.addAttribute("imagenes", imagenes);
        model.addAttribute("titulo", "Carrusel de Imágenes");
        return "carrusel";
    }
    
    // ===== ADMINISTRAR CARRUSEL =====
    @GetMapping("/admin")
    public String administrarCarrusel(Model model) {
        List<ImagenCarrusel> imagenes = imagenCarruselRepository.findAllByOrderByOrdenAsc();
        model.addAttribute("imagenes", imagenes);
        model.addAttribute("nuevaImagen", new ImagenCarrusel());
        return "admin-carrusel";
    }
    
    // ===== AGREGAR IMAGEN (guardando en BD como BLOB) =====
    @PostMapping("/agregar")
    public String agregarImagen(
            @RequestParam("archivoImagen") MultipartFile archivo,
            RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("=== SUBIENDO IMAGEN A IMGBB ===");
            System.out.println("Archivo: " + archivo.getOriginalFilename());
            System.out.println("Tamaño: " + archivo.getSize() + " bytes");
            
            // Validaciones
            if (archivo.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debes seleccionar una imagen");
                return "redirect:/carrusel/admin";
            }
            
            // Validar tipo de archivo
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "El archivo debe ser una imagen");
                return "redirect:/carrusel/admin";
            }
            
            // Validar tamaño (5MB)
            if (archivo.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "La imagen no debe superar los 5MB");
                return "redirect:/carrusel/admin";
            }
            
            // Subir imagen a ImgBB
            String imageUrl = imgBBService.uploadImage(archivo);
            
            // Determinar el siguiente orden disponible
            int nuevoOrden = 1;
            List<ImagenCarrusel> imagenes = imagenCarruselRepository.findAllByOrderByOrdenAsc();
            if (!imagenes.isEmpty()) {
                nuevoOrden = imagenes.get(imagenes.size() - 1).getOrden() + 1;
            }
            
            // Crear entidad
            ImagenCarrusel imagen = new ImagenCarrusel();
            imagen.setImageUrl(imageUrl);
            imagen.setOrden(nuevoOrden);
            imagen.setActiva(true);
            
            // Guardar en PostgreSQL (la imagen va directo a la BD)
            imagenCarruselRepository.save(imagen);
            
            System.out.println("Imagen guardada en BD con ID: " + imagen.getId());
            System.out.println("URL ImgBB: " + imagen.getImageUrl());
            
            redirectAttributes.addFlashAttribute("exito", 
                "✓ Imagen de Carrusel guardada exitosamente y respaldada");
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", 
                "✗ Error al subir la imagen: " + e.getMessage());
        }
        
        return "redirect:/carrusel/admin";
    }

    @GetMapping("/debug-imagenes")
    @ResponseBody
    public String debugImagenes() {
        List<ImagenCarrusel> imagenes = imagenCarruselRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Total imágenes: ").append(imagenes.size()).append("<br><br>");
        
        for (ImagenCarrusel img : imagenes) {
            sb.append("ID: ").append(img.getId()).append("<br>");
            sb.append("URL: <a href='").append(img.getImageUrl()).append("' target='_blank'>Ver imagen</a><br>");
            sb.append("<hr>");
        }
        
        return sb.toString();
    }

    // Método endpoint servirImagen removido porque ahora usamos ImgBB URL directamente
    
    // ===== EDITAR IMAGEN =====
    @PostMapping("/editar/{id}")
    public String editarImagen(
            @PathVariable Long id,
            @RequestParam Integer orden,
            @RequestParam Boolean activa) {
        
        ImagenCarrusel imagen = imagenCarruselRepository.findById(id).orElse(null);
        if (imagen != null) {
            imagen.setOrden(orden);
            imagen.setActiva(activa);
            imagen.setFechaActualizacion(LocalDateTime.now());
            
            imagenCarruselRepository.save(imagen);
        }
        
        return "redirect:/carrusel/admin";
    }
    
    // ===== ELIMINAR IMAGEN =====
    @PostMapping("/eliminar/{id}")
    public String eliminarImagen(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ImagenCarrusel imagen = imagenCarruselRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));
            
            // Solo borrar de la BD, ya no hay archivo físico
            imagenCarruselRepository.deleteById(id);
            
            redirectAttributes.addFlashAttribute("exito", 
                "✓ Imagen eliminada de PostgreSQL");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "✗ Error al eliminar: " + e.getMessage());
        }
        
        return "redirect:/carrusel/admin";
    }

    @PostMapping("/reordenar")
    @ResponseBody
    public Map<String, Object> reordenarImagenes(@RequestBody Map<String, List<String>> payload) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> nuevoOrden = payload.get("orden");
            
            // Validar que tenemos el mismo número de elementos
            if (nuevoOrden == null || nuevoOrden.isEmpty()) {
                response.put("success", false);
                response.put("error", "No se recibió el orden");
                return response;
            }
            
            // Actualizar el orden de cada imagen
            for (int i = 0; i < nuevoOrden.size(); i++) {
                Long id = Long.parseLong(nuevoOrden.get(i));
                ImagenCarrusel imagen = imagenCarruselRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Imagen no encontrada: " + id));
                
                imagen.setOrden(i + 1); // El orden empieza en 1
                imagenCarruselRepository.save(imagen);
            }
            
            response.put("success", true);
            response.put("message", "Orden actualizado correctamente");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
}