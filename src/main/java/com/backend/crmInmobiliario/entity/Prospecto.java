package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
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
    private Boolean destino;
    private Integer cantidadAmbientes;
    private Boolean cochera;
    private Boolean patio;
    private Boolean jardin;
    private Boolean pileta;
    private Boolean visibilidadPublico;
    private Boolean disponible;
    private Boolean mascotas;

    private String norm(String s) {
        if (s == null) return "";
        String x = s.trim().toLowerCase();

        // saca tildes/acentos
        x = Normalizer.normalize(x, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // colapsa espacios
        x = x.replaceAll("\\s+", " ");
        return x;
    }

    public boolean cumpleConPropiedad(Propiedad propiedad) {
        if (propiedad == null) return false;

        // Precio
        if (rangoPrecioMin != null || rangoPrecioMax != null) {
            if (propiedad.getPrecio() == null) return false;

            BigDecimal precioPropiedad = BigDecimal.valueOf(propiedad.getPrecio());
            if (rangoPrecioMin != null && precioPropiedad.compareTo(rangoPrecioMin) < 0) return false;
            if (rangoPrecioMax != null && precioPropiedad.compareTo(rangoPrecioMax) > 0) return false;
        }

        // Zonas
        if (zonaPreferencia != null && !zonaPreferencia.isEmpty()) {

            String localidad = norm(propiedad.getLocalidad());
            String partido = norm(propiedad.getPartido());
            String provincia = norm(propiedad.getProvincia());
            String direccion = norm(propiedad.getDireccion());

            boolean matchZona = zonaPreferencia.stream()
                    .filter(z -> z != null && !z.isBlank())
                    .flatMap(z -> Arrays.stream(norm(z).split("[,;/\\-]"))) // tokeniza "quilmes, bs as"
                    .map(String::trim)
                    .filter(tok -> !tok.isBlank())
                    .anyMatch(tok ->
                            localidad.contains(tok) ||
                                    partido.contains(tok) ||
                                    provincia.contains(tok) ||
                                    direccion.contains(tok)
                    );

            if (!matchZona) return false;
        }

        // Ambientes
        if (cantidadAmbientes != null) {
            if (propiedad.getCantidadAmbientes() == null || propiedad.getCantidadAmbientes() < cantidadAmbientes) {
                return false;
            }
        }

        // Features
        if (Boolean.TRUE.equals(cochera) && !Boolean.TRUE.equals(propiedad.getCochera())) return false;
        if (Boolean.TRUE.equals(patio) && !Boolean.TRUE.equals(propiedad.getPatio())) return false;
        if (Boolean.TRUE.equals(jardin) && !Boolean.TRUE.equals(propiedad.getJardin())) return false;
        if (Boolean.TRUE.equals(pileta) && !Boolean.TRUE.equals(propiedad.getPileta())) return false;

        return true;
    }
}
