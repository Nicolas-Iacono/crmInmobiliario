package com.backend.crmInmobiliario.entity.signature;

import com.backend.crmInmobiliario.entity.Contrato;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "signature_signer")
public class SignatureSigner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contrato contract;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signature_request_id", nullable = false)
    private SignatureRequest signatureRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "signer_role_type", nullable = false, length = 20)
    private SignatureSignerRoleType signerRoleType;

    @Column(name = "related_entity_id", nullable = false)
    private Long relatedEntityId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "dni")
    private String dni;

    @Column(name = "sign_order", nullable = false)
    private Integer signOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SignatureSignerStatus status = SignatureSignerStatus.PENDING;

    @Column(name = "access_token", nullable = false, unique = true, length = 180)
    private String accessToken;

    @Column(name = "access_token_expires_at", nullable = false)
    private LocalDateTime accessTokenExpiresAt;

    @Column(name = "otp_hash", length = 64)
    private String otpHash;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    @Column(name = "otp_validated_at")
    private LocalDateTime otpValidatedAt;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "consent_accepted_at")
    private LocalDateTime consentAcceptedAt;

    @Column(name = "ip_address", length = 120)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Lob
    @Column(name = "evidence_json")
    private String evidenceJson;

    @Lob
    @Column(name = "signature_draw_data_json")
    private String signatureDrawDataJson;

    @Lob
    @Column(name = "signature_image_base64")
    private String signatureImageBase64;
}
