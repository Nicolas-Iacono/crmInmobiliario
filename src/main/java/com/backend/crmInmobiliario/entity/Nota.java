package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Table(name = "notas")
@Entity
@NoArgsConstructor
public class Nota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota")
    private Long id;

    // Relaciones con otras entidades
    @ManyToOne
    @JoinColumn(name = "id_contrado")
    private Contrato contrato; // Relación con el contrato de alquiler

    // Blob puede ser texto o imagen. Podrías considerar usar String si solo es texto.
    @Lob // Esto le dice a JPA que es un campo largo, como CLOB
    @Column(columnDefinition = "TEXT")
    private String contenido;

    private String motivo;

    private EstadoNota estado; // Ej: "Pendiente", "En Proceso", "Resuelto"

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // 🔧 NUEVOS CAMPOS PROPUESTOS

    private LocalDateTime fechaResolucion; // Fecha en que se cerró el reclamo

    private String prioridad; // Ej: "Alta", "Media", "Baja"

    private String tipo; // Ej: "Reparación", "Pago", "Contrato"

    private String observaciones; // Campo adicional para comentarios libres

    @OneToMany(mappedBy = "nota", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageUrls> imagenes = new ArrayList<>();


}
