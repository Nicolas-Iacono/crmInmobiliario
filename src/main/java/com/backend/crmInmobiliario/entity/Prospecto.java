package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@NoArgsConstructor
@Table(name = "prospecto")
public class Prospecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private BigDecimal rangoPrecioMin;
    private BigDecimal rangoPrecioMax;
    private Integer cantidadPersonas;
    private String zonaPreferencia;
    private Integer cantidadAmbientes;
    private Boolean cochera;
    private Boolean patio;
    private Boolean jardin;
    private Boolean pileta;
    private Boolean visibilidadPublico;

    public boolean cumpleConPropiedad(Propiedad propiedad) {
        if (propiedad == null) {
            return false;
        }

        if (zonaPreferencia != null && !zonaPreferencia.isBlank()) {
            String zona = zonaPreferencia.toLowerCase();
            String localidad = propiedad.getLocalidad() != null ? propiedad.getLocalidad().toLowerCase() : "";
            String partido = propiedad.getPartido() != null ? propiedad.getPartido().toLowerCase() : "";
            String provincia = propiedad.getProvincia() != null ? propiedad.getProvincia().toLowerCase() : "";
            String direccion = propiedad.getDireccion() != null ? propiedad.getDireccion().toLowerCase() : "";

            if (!localidad.contains(zona) && !partido.contains(zona) && !provincia.contains(zona) && !direccion.contains(zona)) {
                return false;
            }
        }

        if (cantidadAmbientes != null) {
            if (propiedad.getCantidadAmbientes() == null || propiedad.getCantidadAmbientes() < cantidadAmbientes) {
                return false;
            }
        }

        if (Boolean.TRUE.equals(cochera) && !Boolean.TRUE.equals(propiedad.getCochera())) {
            return false;
        }
        if (Boolean.TRUE.equals(patio) && !Boolean.TRUE.equals(propiedad.getPatio())) {
            return false;
        }
        if (Boolean.TRUE.equals(jardin) && !Boolean.TRUE.equals(propiedad.getJardin())) {
            return false;
        }
        if (Boolean.TRUE.equals(pileta) && !Boolean.TRUE.equals(propiedad.getPileta())) {
            return false;
        }

        return true;
    }
}
