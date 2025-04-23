package com.backend.crmInmobiliario.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "imagenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUrls {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImage;

    private String imageUrl;

    private LocalDateTime fechaSubida = LocalDateTime.now();

    private String nombreOriginal;

    private String tipoImagen; // Ej: "DNI", "Selfie", "Contrato"

    // 🔁 Relación con Garante
//    @ManyToOne
//    @JoinColumn(name = "garante_id")
//    private Garante garante;
//
//    // 🔁 Relación con Inquilino
//    @ManyToOne
//    @JoinColumn(name = "inquilino_id")
//    private Inquilino inquilino;
//
//    // 🔁 Relación con Propietario
//    @ManyToOne
//    @JoinColumn(name = "propietario_id")
//    private Propietario propietario;

    // 🔁 Relación con Propiedad
    @ManyToOne
    @JoinColumn(name = "propiedad_id")
    @JsonIgnore
    private Propiedad propiedad;
}
