package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface PersonaRepository extends JpaRepository<PersonaEntity, Long> {
    
    // Buscar por email
    Optional<PersonaEntity> findByEmail(String email);
    
    // Buscar por nombre
    List<PersonaEntity> findByNombreContainingIgnoreCase(String nombre);
    
    
    // Buscar por fecha de nacimiento
    List<PersonaEntity> findByFechaNacimiento(LocalDate fechaNacimiento);
    
    // Ordenar por fecha
    List<PersonaEntity> findAll();
    
    // PARA PAGINACIÓN - Spring Data JPA ya incluye este método por defecto    
    Page<PersonaEntity> findAllByOrderByFechaRegistroDesc(Pageable pageable);
}