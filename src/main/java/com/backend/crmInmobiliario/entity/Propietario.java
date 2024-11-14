package com.backend.crmInmobiliario.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@DiscriminatorValue("PROPIETARIO")
@Entity
@NoArgsConstructor
@Table(name = "propietarios")
public class Propietario extends Persona{

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @JsonIgnore
    private Usuario usuario;

    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<Propiedad> propiedades;


}
