package com.backend.crmInmobiliario.entity.signature;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "signature_event")
public class SignatureEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signature_request_id", nullable = false)
    private SignatureRequest signatureRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_signer_id")
    private SignatureSigner signer;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private SignatureEventType eventType;

    @CreationTimestamp
    @Column(name = "event_timestamp", nullable = false, updatable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "ip_address", length = 120)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Lob
    @Column(name = "metadata_json")
    private String metadataJson;
}
