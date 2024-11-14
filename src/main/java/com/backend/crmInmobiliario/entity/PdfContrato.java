//package com.backend.crmInmobiliario.entity;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.util.Date;
//
//@Data
//@Table(name = "pdf_contrato")
//@Entity
//@NoArgsConstructor
//public class PdfContrato {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id_pdfContrato")
//    private Long id_pdfContrato;
//
//    @Lob
//    @Column(columnDefinition = "LONGTEXT")
//    private String paragraph;
//
//    @CreationTimestamp
//    @Column
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date fechaCreacion;
//
//    @OneToOne(mappedBy = "pdfContrato", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    private Contrato contrato;
//
//
//}
