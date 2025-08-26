package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.AuthResponse;
import com.backend.crmInmobiliario.DTO.entrada.LoginEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.UserAdminEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ActualizarUsuarioDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.Role;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.exception.UsernameAlreadyExistsException;
import com.backend.crmInmobiliario.repository.USER_REPO.RoleRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IUsuarioService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.JsonPrinter;
import com.backend.crmInmobiliario.utils.JwtUtil;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.*;

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
            throw new UsernameAlreadyExistsException("El username ya se encuentra registrado");
        }

        LOGGER.info("UsuarioEntradaDto: " + JsonPrinter.toString(admin));

        Usuario usuarioEntidad = modelMapper.map(admin, Usuario.class);

        // Encriptar password si aplica
        usuarioEntidad.setPassword(passwordEncoder.encode(admin.getPassword()));

        // Buscar o crear rol ADMIN
        Role adminRole = roleRepository.findByRol(RolesCostantes.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RolesCostantes.ADMIN)));

        usuarioEntidad.setRoles(Collections.singleton(adminRole));

        if (usuarioEntidad.getLogoInmobiliaria() != null) {
            usuarioEntidad.getLogoInmobiliaria().setNota(null);
            usuarioEntidad.getLogoInmobiliaria().setPropiedad(null); // por si acaso
        }
        Usuario usuarioPersistido = usuarioRepository.save(usuarioEntidad);

        // Si necesitas generar un token, hacelo aquí
        // String jwt = jwtUtil.createToken(usuarioPersistido.getUsername(), usuarioPersistido.getRoles());

        return modelMapper.map(usuarioPersistido, TokenDtoSalida.class);
    }


    @Override
    @Transactional
    public UsuarioDtoSalida buscarUsuarioPorId(Long id)  throws IOException, ResourceNotFoundException {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID " + id));

        UsuarioDtoSalida dto = new UsuarioDtoSalida();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setNombreNegocio(usuario.getNombreNegocio());
        dto.setLogo(usuario.getLogoInmobiliaria() != null ? usuario.getLogoInmobiliaria().getImageUrl() : null);
        dto.setEmail(usuario.getEmail());
        dto.setCuit(usuario.getCuit());
        dto.setRazonSocial(usuario.getRazonSocial());
        dto.setPartido(usuario.getPartido());
        dto.setProvincia(usuario.getProvincia());
        dto.setLocalidad(usuario.getLocalidad());
        dto.setMatricula(usuario.getMatricula());
        dto.setTelefono(usuario.getTelefono());
        return dto;
    }

    @Override
    @Transactional
    public UsuarioDtoSalida buscarUsuarioPorUsername(String username) throws ResourceNotFoundException {
        Usuario usuario = usuarioRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con username: " + username));

        UsuarioDtoSalida dto = new UsuarioDtoSalida();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setNombreNegocio(usuario.getNombreNegocio());
        dto.setLogo(usuario.getLogoInmobiliaria() != null ? usuario.getLogoInmobiliaria().getImageUrl() : null);
        dto.setEmail(usuario.getEmail());
        dto.setCuit(usuario.getCuit());
        dto.setRazonSocial(usuario.getRazonSocial());
        dto.setPartido(usuario.getPartido());
        dto.setProvincia(usuario.getProvincia());
        dto.setLocalidad(usuario.getLocalidad());
        dto.setMatricula(usuario.getMatricula());
        dto.setTelefono(usuario.getTelefono());
        return dto;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("usuario no encontrado"));
            return ResponseEntity.ok(usuario);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Usuario no encontrado con ID: " + id, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error inesperado", null));
        }
    }
//  usuario.setLogoInmobiliaria(imagenGuardada);
//
//    ImgUrlSalidaDto dto = new ImgUrlSalidaDto();
//        dto.setIdImage(imagenGuardada.getIdImage());
//        dto.setImageUrl(imagenGuardada.getImageUrl());
//        dto.setNombreOriginal(imagenGuardada.getNombreOriginal());
//        dto.setFechaSubida(imagenGuardada.getFechaSubida());
//        dto.setTipoImagen(imagenGuardada.getTipoImagen());
//
//        usuarioRepository.save(usuario);
//
//        return dto;
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

        // Incluir el userId como claim en el JWT para poder leerlo luego (p.ej. estado de link con Google)
        Long userId = usuarioRepository.findUserByUsername(username)
                .map(Usuario::getId)
                .orElse(null);
        String accesToken = (userId != null)
                ? jwtUtil.createToken(authentication, userId)
                : jwtUtil.createToken(authentication);
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

    @Transactional
    @Override
    public UsuarioDtoSalida actualizarUsuario(Long id, ActualizarUsuarioDto dto) throws ResourceNotFoundException {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID " + id));

        // Actualizar los campos
        if (dto.getNombreNegocio() != null) usuario.setNombreNegocio(dto.getNombreNegocio());
        if (dto.getEmail() != null) usuario.setEmail(dto.getEmail());
        if (dto.getMatricula() != null) usuario.setMatricula(dto.getMatricula());
        if (dto.getRazonSocial() != null) usuario.setRazonSocial(dto.getRazonSocial());
        if (dto.getLocalidad() != null) usuario.setLocalidad(dto.getLocalidad());
        if (dto.getPartido() != null) usuario.setPartido(dto.getPartido());
        if (dto.getProvincia() != null) usuario.setProvincia(dto.getProvincia());
        if (dto.getCuit() != null) usuario.setCuit(dto.getCuit());
        if (dto.getTelefono() != null) usuario.setTelefono(dto.getTelefono());

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return modelMapper.map(usuarioActualizado, UsuarioDtoSalida.class);
    }
}