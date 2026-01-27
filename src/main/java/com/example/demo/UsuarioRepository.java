package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Solo esto - Spring Data JPA provee todos los métodos básicos
    // No necesitas métodos personalizados para el ejemplo simple
}