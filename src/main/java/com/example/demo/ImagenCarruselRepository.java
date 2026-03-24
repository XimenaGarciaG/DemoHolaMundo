package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImagenCarruselRepository extends JpaRepository<ImagenCarrusel, Long> {
    
    List<ImagenCarrusel> findByActivaTrueOrderByOrdenAsc();
    
    List<ImagenCarrusel> findAllByOrderByOrdenAsc();
    
    boolean existsByOrden(Integer orden);
}