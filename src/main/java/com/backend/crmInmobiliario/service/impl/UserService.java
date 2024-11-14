package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.AuthResponse;
import com.backend.crmInmobiliario.DTO.entrada.LoginEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.UserAdminEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Propiedad;
import com.backend.crmInmobiliario.entity.Role;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.RoleRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IUsuarioService;
import com.backend.crmInmobiliario.utils.JsonPrinter;
import com.backend.crmInmobiliario.utils.JwtUtil;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService implements IUsuarioService, UserDetailsService {

    private final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UsuarioRepository usuarioRepository;

    private JwtUtil jwtUtil;

    private PasswordEncoder passwordEncoder;
    private EntityManager entityManager;

    private RoleRepository roleRepository;

    private ModelMapper modelMapper;

    public UserService(UsuarioRepository usuarioRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, EntityManager entityManager, RoleRepository roleRepository, ModelMapper modelMapper) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        configureMapping();
    }

    private void configureMapping() {

        modelMapper.typeMap(UserAdminEntradaDto.class, Usuario.class)
                .addMappings(mapper -> mapper.map(UserAdminEntradaDto::getUsername, Usuario::setUsername));
        modelMapper.typeMap(Usuario.class, TokenDtoSalida.class)
                .addMappings(mapper -> mapper.map(Usuario::getUsername, TokenDtoSalida::setUsername));
    }

    @Transactional
    @Override
    public List<UsuarioDtoSalida> listarUsuarios() {
        List<UsuarioDtoSalida> usuarioDtoSalidas = usuarioRepository.findAll()
                .stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDtoSalida.class))
                .toList();
        return usuarioDtoSalidas;
    }

    @Transactional
    @Override
    public TokenDtoSalida registrarUsuarioAdmin(UserAdminEntradaDto admin) {
        if (usuarioRepository.existsByUsername(admin.getUsername())) {
            throw new RuntimeException("El username ya se encuentra registrado");
        }

        LOGGER.info("UsuarioEntradaDto: " + JsonPrinter.toString(admin));

        // Mapea el DTO de entrada a la entidad User
        Usuario usuarioEntidad = modelMapper.map(admin, Usuario.class);

        // Asigna el rol ADMIN al usuario
        Role adminRole = roleRepository.findByRol(RolesCostantes.ADMIN)
                .orElseGet(() -> new Role(RolesCostantes.ADMIN));

        if (adminRole.getIdRol() == null) {
            adminRole = roleRepository.save(adminRole);  // Guarda el rol si no existe
        }

        // Asegúrate de que el rol esté gestionado por el EntityManager
        adminRole = entityManager.merge(adminRole);

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        usuarioEntidad.setRoles(roles);

        // Guarda el usuario en el repositorio
        Usuario usuarioAPersistir = usuarioRepository.save(usuarioEntidad);

        TokenDtoSalida tokenSalidaDto = modelMapper.map(usuarioAPersistir, TokenDtoSalida.class);

        // Crea y retorna el DTO de salida con el token
        new TokenDtoSalida(
                usuarioAPersistir.getId(),
                usuarioAPersistir.getUsername(),
                usuarioAPersistir.getEmail(),
                usuarioAPersistir.getNombreNegocio(),
                usuarioAPersistir.getContratos(),
                usuarioAPersistir.getInquilinos(),
                usuarioAPersistir.getPropietarios(),
                usuarioAPersistir.getPropiedades(),
                usuarioAPersistir.getGarantes(),
                new ArrayList<>(usuarioAPersistir.getRoles())
        );

        return tokenSalidaDto;
    }

    @Override
    public UsuarioDtoSalida buscarUsuarioPorId(Long id) {
        return null;
    }

    @Override
    public void eliminarUsuario(Long id) {

    }

    @Override
    public UsuarioDtoSalida buscarUsuarioPorEmail(String email) {
        return null;
    }

    @Override
    public AuthResponse loginUser(LoginEntradaDto loginEntradaDto) {

        String username = loginEntradaDto.username();
        String password = loginEntradaDto.password();

        Authentication authentication = this.authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accesToken = jwtUtil.createToken(authentication);
        AuthResponse authResponse = new AuthResponse(username, "usuario creado correctamente", accesToken, true);
        return authResponse;
    }



    public Authentication authenticate(String username, String password) {
        UserDetails userDetails = this.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException("usuario incorrecto o no registrado.");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("contraseña incorrecta.");
        }
        return new UsernamePasswordAuthenticationToken(username, userDetails.getPassword(), userDetails.getAuthorities());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario user = usuarioRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario " + username + " no existe"));

        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        user.getRoles().forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_" + role.getRol())));
        user.getRoles().stream()
                .flatMap(role -> role.getPermisosList().stream())
                .forEach(permission -> authorityList.add(new SimpleGrantedAuthority(permission.getName())));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isAccountNonLocked(),
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.isAccountNonExpired(),
                authorityList
        );
    }
}