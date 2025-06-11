package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Table(name = "usuario")
@Entity
@NoArgsConstructor
public class Usuario  implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String nombreNegocio;
    private String email;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private ImageUrls logoInmobiliaria;

    // Relación con las demás entidades
    @OneToMany(mappedBy = "usuario")
    private List<Inquilino> inquilinos;

    @OneToMany(mappedBy = "usuario")
    private List<Propietario> propietarios;

    @OneToMany(mappedBy = "usuario")
    private List<Propiedad> propiedades;

    @OneToMany(mappedBy = "usuario")
    private List<Contrato> contratos;

    @OneToMany(mappedBy = "usuario")
    private List<Garante> garantes;

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = Role.class, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name="users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
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


    // Getters y setters
}

