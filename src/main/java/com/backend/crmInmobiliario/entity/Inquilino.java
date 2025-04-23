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
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
//
//    @OneToMany(mappedBy = "inquilino", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ImageUrls> imagenes = new ArrayList<>();
}
