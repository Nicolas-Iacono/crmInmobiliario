package com.backend.crmInmobiliario.entity;

import com.backend.crmInmobiliario.entity.impuestos.Agua;
import com.backend.crmInmobiliario.entity.impuestos.Gas;
import com.backend.crmInmobiliario.entity.impuestos.Luz;
import com.backend.crmInmobiliario.entity.impuestos.Municipal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "recibos")
@NoArgsConstructor
public class Recibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recibo")
    private Long id;

    @CreationTimestamp
    private LocalDate fechaEmision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrato", nullable = false)
    @JsonIgnore // Evita referencias circulares en la serialización
    private Contrato contrato;
    private int numeroRecibo;
    private String periodo;
    private String concepto;
    private BigDecimal montoTotal;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    // Relación polimórfica con Impuesto
    @JsonManagedReference // Anotación en la entidad "padre"
    @OneToMany(mappedBy = "recibo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Impuesto> impuestos = new ArrayList<>();

    private Boolean estado;

    @PrePersist
    public void prePersist() {
        if (this.fechaVencimiento == null) {
            this.fechaVencimiento = LocalDate.now().plusDays(15);
        }
    }
}
