package com.backend.crmInmobiliario.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.w3c.dom.Text;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

@Data
@Table(name = "propiedad")
@Entity
@NoArgsConstructor
public class Propiedad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_propiedad")
    public Long id_propiedad;
    private String direccion;
    private String localidad;
    private String partido;
    private String provincia;
    private Boolean disponibilidad;

    @Column(name = "visible_a_otros", nullable = false)
    private boolean visibleAOtros = false;

    private Integer cantidadAmbientes;
    private Boolean pileta;
    private Boolean cochera;
    private Boolean jardin;
    private Boolean patio;
    private Boolean balcon;
    private Double precio;

    @Enumerated(EnumType.STRING)
    @Column(name = "moneda", nullable = false)
    private MonedaPropiedad moneda = MonedaPropiedad.ARS;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false)
    private TipoOperacionPropiedad tipoOperacion = TipoOperacionPropiedad.ALQUILER;

    // Campo largo para el inventario
    @Lob
    @Column(name = "inventario", length = 3000)
    private String inventario;
    private String tipo;


    @ManyToOne
    @JoinColumn(name = "propietario_id", nullable = true)
    @ToString.Exclude
    private Propietario propietario;

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ImageUrls> imagenes = new ArrayList<>();

    @ManyToOne
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public boolean isDisponibilidad() {
        return disponibilidad;
    }
    public void setDisponibilidad(boolean disponibilidad) {
        this.disponibilidad = disponibilidad;
    }
}
