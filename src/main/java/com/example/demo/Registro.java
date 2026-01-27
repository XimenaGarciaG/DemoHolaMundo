package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "REGISTROS")
public class Registro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    @Column(name = "FECHA_REGISTRO")
    private LocalDateTime fechaRegistro;
    
    @Column(name = "NOMBRE")
    private String nombre;
    
    // Constructores
    public Registro() {
        this.fechaRegistro = LocalDateTime.now();
    }
    
    public Registro(String nombre) {
        this.nombre = nombre;
        this.fechaRegistro = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    @Override
    public String toString() {
        return "Registro{" +
                "id=" + id +
                ", fechaRegistro=" + fechaRegistro +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}