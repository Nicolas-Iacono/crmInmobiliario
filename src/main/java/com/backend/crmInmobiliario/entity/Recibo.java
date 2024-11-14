package com.backend.crmInmobiliario.entity;

import com.backend.crmInmobiliario.entity.impuestos.Agua;
import com.backend.crmInmobiliario.entity.impuestos.Gas;
import com.backend.crmInmobiliario.entity.impuestos.Luz;
import com.backend.crmInmobiliario.entity.impuestos.Municipal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Table(name = "recibos")
@Entity
@NoArgsConstructor
public class Recibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recibo")
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "id_contrato", nullable = false)
//    private Contrato contrato;
//
//    @ManyToOne
//    @ToString.Exclude
//    @JsonIgnore
//    @JoinColumn(name = "usuario_id")
//    private Usuario usuario;

    @CreationTimestamp
    private LocalDate fechaEmision;

    private LocalDate periodo;

    private BigDecimal montoTotal;

    private Double aguaServicio;
    private Double luzServicio;
    private Double gasServicio;
    private Double municipalServicio;


}
