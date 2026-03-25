package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface PersonaRepository extends JpaRepository<PersonaEntity, Long>, JpaSpecificationExecutor<PersonaEntity> {
    
    // Buscar por email
    Optional<PersonaEntity> findByEmail(String email);
    
    // Buscar por nombre
    List<PersonaEntity> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar por fecha de nacimiento
    List<PersonaEntity> findByFechaNacimiento(LocalDate fechaNacimiento);
    
    // === CONSULTAS PARA FILTROS DINÁMICOS ===
    
    @Query("SELECT DISTINCT UPPER(SUBSTRING(p.nombre, 1, 1)) FROM PersonaEntity p WHERE p.nombre IS NOT NULL ORDER BY 1")
    List<String> findUniqueInitials();
    
    @Query("SELECT DISTINCT EXTRACT(YEAR FROM p.fechaNacimiento) FROM PersonaEntity p WHERE p.fechaNacimiento IS NOT NULL ORDER BY 1")
    List<Integer> findUniqueYears();
    
    @Query("SELECT DISTINCT EXTRACT(MONTH FROM p.fechaNacimiento) FROM PersonaEntity p WHERE p.fechaNacimiento IS NOT NULL ORDER BY 1")
    List<Integer> findUniqueMonths();
    
    @Query("SELECT DISTINCT p.estadoCivil FROM PersonaEntity p WHERE p.estadoCivil IS NOT NULL AND p.estadoCivil <> '' ORDER BY 1")
    List<String> findUniqueMaritalStatuses();
    
    @Query(value = "SELECT DISTINCT split_part(email, '@', 2) FROM personas WHERE email LIKE '%@%' ORDER BY 1", nativeQuery = true)
    List<String> findUniqueDomains();
    
    // Ordenar por fecha
    List<PersonaEntity> findAll();
    
    // PARA PAGINACIÓN - Spring Data JPA ya incluye este método por defecto    
    Page<PersonaEntity> findAllByOrderByFechaRegistroDesc(Pageable pageable);
}