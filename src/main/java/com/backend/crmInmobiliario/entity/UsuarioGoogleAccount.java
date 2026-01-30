package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "usuario_google_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_google_sub", columnNames = {"google_sub"}),
                @UniqueConstraint(name = "uk_user_google", columnNames = {"usuario_id"})
        }
)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UsuarioGoogleAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @ToString.Include
    @Column(name = "google_sub", nullable = false, length = 64)
    private String googleSub;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "picture_url", length = 512)
    private String pictureUrl;

    @Column(name = "scope", length = 1024)
    private String scope;

    // Access token suele no persistirse; si lo hacés, guardá expiración.
    @Column(name = "access_token", length = 2048)
    private String accessToken;

    @Column(name = "access_token_expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date accessTokenExpiresAt;

    // REFRESH TOKEN: cifrado/ ofuscado en repositorio/AttributeConverter
    @Column(name = "refresh_token", length = 2048)
    private String refreshToken;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "locale", length = 16)
    private String locale;

    @Column(name = "linked_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date linkedAt;
}
