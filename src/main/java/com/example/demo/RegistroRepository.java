package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroRepository extends JpaRepository<Registro, Long> {
    // Métodos personalizados si los necesitas
    long count(); // Ya está incluido en JpaRepository
}