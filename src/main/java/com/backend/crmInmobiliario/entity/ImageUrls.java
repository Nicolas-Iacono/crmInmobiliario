package com.backend.crmInmobiliario.entity;


import com.backend.crmInmobiliario.entity.oficios.OficioProveedor;
import com.backend.crmInmobiliario.entity.oficios.OficioServicio;
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

    // 🔁 Relación con Propiedad
    @ManyToOne
    @JoinColumn(name = "propiedad_id")
    @JsonIgnore
    private Propiedad propiedad;

    //relacion con nota
    @ManyToOne
    @JoinColumn(name = "nota_id")
    @JsonIgnore
    private Nota nota;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    @JsonIgnore
    private OficioServicio servicio;

    @OneToOne
    @JoinColumn(name = "proveedor_id")
    @JsonIgnore
    private OficioProveedor proveedor;

}
