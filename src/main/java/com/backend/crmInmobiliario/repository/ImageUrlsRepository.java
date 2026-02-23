package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.ImageUrls;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageUrlsRepository extends JpaRepository<ImageUrls, Long> {
    List<ImageUrls> findByServicioId(Long servicioId);
    Optional<ImageUrls> findByIdImageAndServicioId(Long idImage, Long servicioId);
    void deleteByServicioId(Long servicioId);
}
