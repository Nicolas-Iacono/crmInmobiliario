package com.backend.crmInmobiliario.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@DiscriminatorValue("GARANTE")
@Entity
@NoArgsConstructor
@Table(name = "garantes")
public class Garante extends Persona{

    @ManyToOne
    @JoinColumn(name = "id_contrato")
    private Contrato contrato;

    @OneToMany(mappedBy = "garante", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<ImageUrls> imageUrls = new ArrayList<>();

    private String tipoGarantia;

    private String nombreEmpresa;
    private String sectorActual;
    private String cargoActual;
    private Long legajo;
    private String cuitEmpresa;

    private String partidaInmobiliaria;
    private String direccion;
    private String infoCatastral;
    private String estadoOcupacion;
    private String tipoPropiedad;
    private String informeDominio;
    private String informeInhibicion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @JsonIgnore
    private Usuario usuario;
}
