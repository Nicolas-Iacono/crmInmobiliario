package com.backend.crmInmobiliario.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Table(name = "IMAGE_URLS")
@AllArgsConstructor
@NoArgsConstructor
public class ImageUrls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_image")
    private Long idImage;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_garante")
    @ToString.Exclude
    private Garante garante;
}
