package com.example.demo;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

public class PersonaForm {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    private String email;
    
    @NotNull(message = "La edad es obligatoria")
    @Min(value = 1, message = "La edad mínima es 1 año")
    @Max(value = 120, message = "La edad máxima es 120 años")
    private Integer edad;
    
    @Pattern(regexp = "^\\d{10}$", message = "El teléfono debe tener 10 dígitos")
    private String telefono;
    
    @Pattern(regexp = "^[A-Z]{4}[0-9]{6}[A-Z0-9]{3}$", 
             message = "El CURP debe tener formato válido (18 caracteres alfanuméricos)")
    private String curp;
    
    @Pattern(regexp = "^[A-Z&Ñ]{3,4}[0-9]{6}[A-Z0-9]{3}$", 
             message = "El RFC debe tener formato válido")
    private String rfc;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Length(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "La contraseña debe contener mayúsculas, minúsculas y números")
    private String password;
    
    @NotBlank(message = "Debes confirmar la contraseña")
    private String confirmPassword;
    
    @AssertTrue(message = "Debes aceptar los términos y condiciones")
    private Boolean aceptaTerminos;
    
    // Getters y Setters (igual que en el controlador, pero separados)
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getCurp() { return curp; }
    public void setCurp(String curp) { this.curp = curp; }
    
    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    
    public Boolean getAceptaTerminos() { return aceptaTerminos; }
    public void setAceptaTerminos(Boolean aceptaTerminos) { this.aceptaTerminos = aceptaTerminos; }
}