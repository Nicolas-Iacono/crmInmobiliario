package com.backend.crmInmobiliario.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@Table(name = "presupuesto")
@NoArgsConstructor
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "usuario_id")  // Ajusta el nombre de la columna según tu esquema de base de datos
    private Usuario usuario;

    private String titulo;

    /** Monto mensual base */
    private Double monto;

    /** Porcentaje en string, ej "3.5" */
    private String porcentajeContrato;

    /** Porcentaje en string, ej "1.2" */
    private String porcentajeSello;

    /** Meses de contrato */
    private int duracion;

    private Double gastosExtras;
}
