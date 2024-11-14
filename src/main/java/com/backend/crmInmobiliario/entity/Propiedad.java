package com.backend.crmInmobiliario.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.w3c.dom.Text;

import java.sql.Blob;

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
    // Campo largo para el inventario
    @Lob
    @Column(name = "inventario", length = 3000)
    private String inventario;
    private String tipo;


    @ManyToOne
    @JoinColumn(name = "propietario_id", nullable = false)
    @ToString.Exclude
    private Propietario propietario;

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
