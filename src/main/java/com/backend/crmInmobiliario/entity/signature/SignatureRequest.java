package com.backend.crmInmobiliario.entity.signature;

import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "signature_request")
public class SignatureRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contrato contract;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private Usuario createdByUser;

    @Column(name = "pdf_hash_sha256", nullable = false, length = 64)
    private String pdfHashSha256;

    @Column(name = "pdf_original_url", nullable = false, length = 1200)
    private String pdfOriginalUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SignatureRequestStatus status = SignatureRequestStatus.PENDING;

    @Column(name = "sequential_signing", nullable = false)
    private boolean sequentialSigning;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "signatureRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("signOrder ASC, id ASC")
    private List<SignatureSigner> signers = new ArrayList<>();

    @OneToMany(mappedBy = "signatureRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("eventTimestamp ASC, id ASC")
    private List<SignatureEvent> events = new ArrayList<>();
}
