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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "recibos",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_recibo_contrato_periodo", columnNames={"contrato_id","periodo"})
        })
@NoArgsConstructor
public class Recibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recibo")
    private Long id;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrato", nullable = false)
    @JsonIgnore
    private Contrato contrato;

    private int numeroRecibo;

    private String periodo;

    private String concepto;

    private BigDecimal montoTotal;

    @ManyToOne
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "usuario_id")  // Ajusta el nombre de la columna según tu esquema de base de datos
    private Usuario usuario;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    // Relación polimórfica con Impuesto
    @JsonManagedReference // Anotación en la entidad "padre"
    @OneToMany(mappedBy = "recibo", cascade = CascadeType.ALL, fetch = FetchType.LAZY,  orphanRemoval = true)
    private List<Impuesto> impuestos = new ArrayList<>();

    private Boolean estado = Boolean.FALSE;

    @Column(name = "mp_preference_id")
    private String mpPreferenceId;

    @Column(name = "mp_payment_id")
    private String mpPaymentId;

    @Column(name = "mp_status")
    private String mpStatus;

    @Column(name = "mp_external_reference")
    private String mpExternalReference;

    @Column(name = "mp_paid_at")
    private LocalDateTime mpPaidAt;

    @Column(name = "liquidado", nullable = false)
    private Boolean liquidado = Boolean.FALSE;

    @PrePersist
    public void prePersist() {
        if (this.fechaVencimiento == null) {
            this.fechaVencimiento = LocalDate.now().plusDays(10);
        }
        if (this.estado == null) this.estado = Boolean.FALSE;
        if (this.liquidado == null) this.liquidado = Boolean.FALSE;
        if (this.transferStatus == null) this.transferStatus = TransferStatus.NONE;
    }

    @Column(name = "transfer_alias")
    private String transferAlias;

    @Column(name = "transfer_amount")
    private BigDecimal transferAmount;

    @Column(name = "transfer_notified_at")
    private LocalDateTime transferNotifiedAt;

    @Column(name = "transfer_reference")
    private String transferReference; // ej: nro operación / últimos 4 / etc

    @Column(name = "transfer_comprobante_url")
    private String transferComprobanteUrl;

    @Column(name = "transfer_note", length = 500)
    private String transferNote;

    private TransferStatus transferStatus = TransferStatus.NONE;

    @Column(name="paid_at")
    private LocalDateTime paidAt;






}
