package com.backend.crmInmobiliario.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "lead", uniqueConstraints = {
        @UniqueConstraint(name = "uk_lead_call_id", columnNames = "call_id")
})
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_id", nullable = false, unique = true)
    private String callId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    private String modo;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    private String telefono;
    private String email;
    private String presupuesto;

    @Column(name = "zona_interes")
    private String zonaInteres;

    @Column(name = "tipo_propiedad")
    private String tipoPropiedad;

    @Column(name = "contexto", columnDefinition = "TEXT")
    private String contexto;

    private String timing;
    private String garantia;

    @Column(name = "nivel_lead")
    private String nivelLead;

    @Column(name = "proximo_paso")
    private String proximoPaso;

    @Column(name = "objeciones_json", columnDefinition = "TEXT")
    private String objecionesJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
