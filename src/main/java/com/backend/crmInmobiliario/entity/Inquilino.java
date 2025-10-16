package com.backend.crmInmobiliario.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@DiscriminatorValue("INQUILINO")
@Entity
@NoArgsConstructor
@Table(name = "inquilinos")
public class Inquilino extends Persona{

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @JsonIgnore // ⛔ evita ciclos en JSON
    private Usuario usuario; // propietario del sistema

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cuenta_id")
    @ToString.Exclude
    @JsonIgnore // ⛔ evita recursión con usuarioCuentaInquilino
    private Usuario usuarioCuentaInquilino;
//
//    @OneToMany(mappedBy = "inquilino", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ImageUrls> imagenes = new ArrayList<>();
}
