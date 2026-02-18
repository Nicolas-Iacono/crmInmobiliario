package com.backend.crmInmobiliario.entity.oficios;

import com.backend.crmInmobiliario.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "oficio_calificacion", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"oficio_proveedor_id", "inmobiliaria_id"})
})
@Getter
@Setter
public class OficioCalificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oficio_proveedor_id", nullable = false)
    private OficioProveedor proveedor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inmobiliaria_id", nullable = false)
    private Usuario inmobiliaria;

    @Column(nullable = false)
    private Integer puntaje;

    private String comentario;
}
