package com.backend.crmInmobiliario.entity;

import com.backend.crmInmobiliario.entity.impuestos.Agua;
import com.backend.crmInmobiliario.entity.impuestos.Gas;
import com.backend.crmInmobiliario.entity.impuestos.Luz;
import com.backend.crmInmobiliario.entity.impuestos.Municipal;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Table(name = "contrato")
@Entity
@NoArgsConstructor
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Long id_contrato;
    private String nombreContrato;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;

    @ManyToOne
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "usuario_id")  // Ajusta el nombre de la columna según tu esquema de base de datos
    private Usuario usuario;

    @CreationTimestamp
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date publicDate;
//    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @JoinColumn(name = "pdf_contrato_id")  // Columna en la tabla 'contrato' que hace referencia a la clave primaria de 'PdfContrato'
//    private PdfContrato pdfContrato;
@Lob
@Column(name = "inventario", length = 20000)
    private String pdfContratoTexto;

    @ManyToOne
    @JoinColumn(name = "id_propietario", nullable = false)
    private Propietario propietario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_inquilino", nullable = false)
    private Inquilino inquilino;

    @ManyToOne
    @JoinColumn(name = "id_propiedad", nullable = false)
    private Propiedad propiedad;

    @OneToMany(mappedBy = "contrato", fetch = FetchType.EAGER)
    private List<Garante> garantes;

    @ManyToOne
    @JoinColumn(name = "id_municipal", referencedColumnName = "id")
    private Municipal municipal;

    @ManyToOne
    @JoinColumn(name = "id_gas", referencedColumnName = "id")
    private Gas gas;

    @ManyToOne
    @JoinColumn(name = "id_luz", referencedColumnName = "id") 
    private Luz luz;

    @ManyToOne
    @JoinColumn(name = "id_agua",referencedColumnName = "id")
    private Agua agua;

    private int actualizacion;
    private double montoAlquiler;
    private String montoAlquilerLetras;
    private Double multaXDia;
    private boolean activo;
    private String indiceAjuste;
    private int duracion;
    private Long tiempoRestante;
    private String destino;

//    @OneToMany(mappedBy = "contrato", fetch = FetchType.LAZY)
//    private List<Recibo> recibos;



    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
