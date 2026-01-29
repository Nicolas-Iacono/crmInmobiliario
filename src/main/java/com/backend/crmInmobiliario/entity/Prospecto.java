package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    @ElementCollection
    @CollectionTable(name = "prospecto_zonas", joinColumns = @JoinColumn(name = "prospecto_id"))
    @Column(name = "zona_preferencia")
    private List<String> zonaPreferencia = new ArrayList<>();
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

        if (rangoPrecioMin != null || rangoPrecioMax != null) {
            if (propiedad.getPrecio() == null) {
                return false;
            }
            BigDecimal precioPropiedad = BigDecimal.valueOf(propiedad.getPrecio());
            if (rangoPrecioMin != null && precioPropiedad.compareTo(rangoPrecioMin) < 0) {
                return false;
            }
            if (rangoPrecioMax != null && precioPropiedad.compareTo(rangoPrecioMax) > 0) {
                return false;
            }
        }

        if (zonaPreferencia != null && !zonaPreferencia.isEmpty()) {

            String localidad = propiedad.getLocalidad() != null ? propiedad.getLocalidad().toLowerCase() : "";
            String partido = propiedad.getPartido() != null ? propiedad.getPartido().toLowerCase() : "";
            String provincia = propiedad.getProvincia() != null ? propiedad.getProvincia().toLowerCase() : "";
            String direccion = propiedad.getDireccion() != null ? propiedad.getDireccion().toLowerCase() : "";

            boolean matchZona = zonaPreferencia.stream()
                    .filter(zona -> zona != null && !zona.isBlank())
                    .map(String::toLowerCase)
                    .anyMatch(zona -> localidad.contains(zona)
                            || partido.contains(zona)
                            || provincia.contains(zona)
                            || direccion.contains(zona));

            if (!matchZona) {
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
