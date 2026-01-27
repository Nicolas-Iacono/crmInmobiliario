package com.backend.crmInmobiliario.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@DiscriminatorValue("PROPIETARIO")
@Entity
@NoArgsConstructor
@Table(name = "propietarios")
@JsonIgnoreProperties({"propiedades"})
public class Propietario extends Persona{

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @JsonIgnore
    private Usuario usuario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cuenta_propietario_id")
    @ToString.Exclude
    @JsonIgnore // ⛔ evita recursión con usuarioCuentaInquilino
    private Usuario usuarioCuentaPropietario;


    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Propiedad> propiedades = new ArrayList<>();

    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<Documento> documentos = new ArrayList<>();


}
