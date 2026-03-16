package com.backend.crmInmobiliario.entity;

import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "usuario",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_email", columnNames = "email")
        }
)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario  implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ToString.Include
    private String username;

    private String password;
    private String nombreNegocio;
    private String email;
    private String matricula;
    private String razonSocial;
    private String localidad;
    private String partido;
    private String provincia;
    private String cuit;
    private String telefono;
    private String colegio;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    @ToString.Exclude
    private ImageUrls logoInmobiliaria;

    @OneToMany(mappedBy = "usuario")
    @ToString.Exclude
    private List<Inquilino> inquilinos;

    @OneToMany(mappedBy = "usuario")
    @ToString.Exclude
    private List<Propietario> propietarios;

    @OneToMany(mappedBy = "usuario")
    @ToString.Exclude
    private List<Propiedad> propiedades;

    @OneToMany(mappedBy = "usuario")
    @ToString.Exclude
    private List<Contrato> contratos;

    @OneToMany(mappedBy = "usuario")
    @ToString.Exclude
    private List<Garante> garantes;

    private String googleId;
    private String googleEmail;

    @OneToMany(mappedBy = "usuario")
    @ToString.Exclude
    private List<Recibo> recibos;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OficioResena> resenasRealizadas = new ArrayList<>();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private UsuarioGoogleAccount googleAccount;


    @Column(name = "mp_connected", nullable = false)
    private boolean mpConnected = false;

    @Column(name="mp_access_token")
    private String mpAccessToken;

    @Column(name="mp_refresh_token")
    private String mpRefreshToken;

    @Column(name="mp_token_expires_at")
    private LocalDateTime mpTokenExpiresAt;

    // Datos de cobro por transferencia
    @Column(name = "mp_alias")
    private String mpAlias;

    @Column(name = "mp_cbu")
    private String mpCbu;

    @Column(name = "mp_titular")
    private String mpTitular;

    @Column(name = "mp_cuit")
    private String mpCuit;

    @Column(name = "mp_banco")
    private String mpBanco;

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = Role.class, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role.getRol())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @ToString.Exclude
    @JsonIgnore
    private Plan plan;

    @Column(name = "es_admin", nullable = false)
    private boolean esAdmin = false;

    @OneToOne
    @JoinColumn(name = "inquilino_id")
    @ToString.Exclude
    @JsonIgnore // 🔥 evita ciclo con Inquilino
    private Inquilino inquilino;

    @OneToOne
    @JoinColumn(name = "propietario_id")
    @ToString.Exclude
    @JsonIgnore // 🔥 evita ciclo con Propietario
    private Propietario propietario;

    @OneToOne
    @JoinColumn(name = "garante_id")
    @ToString.Exclude
    @JsonIgnore // 🔥 evita ciclo con Propietario
    private Garante garante;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<PlantillaContrato> plantillasContrato = new ArrayList<>();

    // Getters y setters


}

