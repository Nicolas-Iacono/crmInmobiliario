package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.AuthResponse;
import com.backend.crmInmobiliario.DTO.entrada.LoginEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.UserAdminEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.usuarioInquilino.LoginInquilinoEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.usuarioPropietario.LoginPropietarioEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ActualizarUsuarioDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.entrada.UsuarioCobroTransferenciaDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.modificacion.DatosCobroUpdateDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.salida.DatosCobroSoloUser;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.Recibo;
import com.backend.crmInmobiliario.entity.Role;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.exception.UsernameAlreadyExistsException;
import com.backend.crmInmobiliario.repository.USER_REPO.RoleRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IUsuarioService;
import com.backend.crmInmobiliario.service.impl.nodeMailer.EmailService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.JsonPrinter;
import com.backend.crmInmobiliario.utils.JwtUtil;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

import java.io.IOException;
import java.util.*;

@Service
public class UserService implements IUsuarioService, UserDetailsService {

    private final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private final UsuarioRepository usuarioRepository;

    private final EmailService emailService;

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    private final RoleRepository roleRepository;

    private final ModelMapper modelMapper;

    public UserService(EmailService emailService, UsuarioRepository usuarioRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, EntityManager entityManager, RoleRepository roleRepository, ModelMapper modelMapper) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        this.emailService = emailService;
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
    public TokenDtoSalida registrarUsuario(UserAdminEntradaDto usuario ) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new UsernameAlreadyExistsException("El username ya se encuentra registrado");
        }
        validarEmailUnicoEnAlta(usuario.getEmail());

        LOGGER.info("UsuarioEntradaDto: " + JsonPrinter.toString(usuario));

        // ⛔️ sacar este map que explota:
        // Usuario usuarioEntidad = modelMapper.map(admin, Usuario.class);

        // ✅ map manual y explícito
        Usuario usuarioEntidad = new Usuario();
        usuarioEntidad.setUsername(usuario.getUsername());
        usuarioEntidad.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioEntidad.setNombreNegocio(usuario.getNombreNegocio());
        usuarioEntidad.setEmail(normalizarEmail(usuario.getEmail()));
        usuarioEntidad.setMatricula(usuario.getMatricula());
        usuarioEntidad.setRazonSocial(usuario.getRazonSocial());
        usuarioEntidad.setLocalidad(usuario.getLocalidad());
        usuarioEntidad.setPartido(usuario.getPartido());
        usuarioEntidad.setProvincia(usuario.getProvincia());
        usuarioEntidad.setCuit(usuario.getCuit());
        usuarioEntidad.setTelefono(usuario.getTelefono());
        usuarioEntidad.setColegio(usuario.getColegio());

        Role adminRole = roleRepository.findByRol(RolesCostantes.SUPER_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RolesCostantes.SUPER_ADMIN)));
        usuarioEntidad.setRoles(Collections.singleton(adminRole));

        if (usuarioEntidad.getLogoInmobiliaria() != null) {
            usuarioEntidad.getLogoInmobiliaria().setNota(null);
            usuarioEntidad.getLogoInmobiliaria().setPropiedad(null);
        }

        Usuario usuarioPersistido = usuarioRepository.save(usuarioEntidad);

        return modelMapper.map(usuarioPersistido, TokenDtoSalida.class);
    }

    @Transactional
    @Override
    public TokenDtoSalida registrarUsuarioAdmin(UserAdminEntradaDto admin) {
        if (usuarioRepository.existsByUsername(admin.getUsername())) {
            throw new UsernameAlreadyExistsException("El username ya se encuentra registrado");
        }
        validarEmailUnicoEnAlta(admin.getEmail());

        LOGGER.info("UsuarioEntradaDto: " + JsonPrinter.toString(admin));



        // ✅ map manual y explícito
        Usuario usuarioEntidad = new Usuario();
        usuarioEntidad.setUsername(admin.getUsername());
        usuarioEntidad.setPassword(passwordEncoder.encode(admin.getPassword()));
        usuarioEntidad.setNombreNegocio(admin.getNombreNegocio());
        usuarioEntidad.setEmail(normalizarEmail(admin.getEmail()));
        usuarioEntidad.setMatricula(admin.getMatricula());
        usuarioEntidad.setRazonSocial(admin.getRazonSocial());
        usuarioEntidad.setLocalidad(admin.getLocalidad());
        usuarioEntidad.setPartido(admin.getPartido());
        usuarioEntidad.setProvincia(admin.getProvincia());
        usuarioEntidad.setCuit(admin.getCuit());
        usuarioEntidad.setTelefono(admin.getTelefono());
        usuarioEntidad.setColegio(admin.getColegio());

        Role adminRole = roleRepository.findByRol(RolesCostantes.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RolesCostantes.ADMIN)));
        usuarioEntidad.setRoles(Collections.singleton(adminRole));

        if (usuarioEntidad.getLogoInmobiliaria() != null) {
            usuarioEntidad.getLogoInmobiliaria().setNota(null);
            usuarioEntidad.getLogoInmobiliaria().setPropiedad(null);
        }

        Usuario usuarioPersistido = usuarioRepository.save(usuarioEntidad);

        try {
            emailService.enviarConfirmacionRegistro(
                    usuarioPersistido.getUsername(),
                    usuarioPersistido.getEmail()
            );
            LOGGER.info("Email de confirmación enviado a: " + usuarioPersistido.getEmail());
        } catch (Exception e) {
            LOGGER.error("Error al enviar email de confirmación: ", e);
        }
        return modelMapper.map(usuarioPersistido, TokenDtoSalida.class);
    }
    @Transactional
    @Override
    public TokenDtoSalida registrarUsuarioSuperAdmin(UserAdminEntradaDto superAdmin ) {
        if (usuarioRepository.existsByUsername(superAdmin.getUsername())) {
            throw new UsernameAlreadyExistsException("El username ya se encuentra registrado");
        }
        validarEmailUnicoEnAlta(superAdmin.getEmail());

        LOGGER.info("UsuarioEntradaDto: " + JsonPrinter.toString(superAdmin));

        // ⛔️ sacar este map que explota:
        // Usuario usuarioEntidad = modelMapper.map(admin, Usuario.class);

        // ✅ map manual y explícito
        Usuario usuarioEntidad = new Usuario();
        usuarioEntidad.setUsername(superAdmin.getUsername());
        usuarioEntidad.setPassword(passwordEncoder.encode(superAdmin.getPassword()));
        usuarioEntidad.setNombreNegocio(superAdmin.getNombreNegocio());
        usuarioEntidad.setEmail(normalizarEmail(superAdmin.getEmail()));
        usuarioEntidad.setMatricula(superAdmin.getMatricula());
        usuarioEntidad.setRazonSocial(superAdmin.getRazonSocial());
        usuarioEntidad.setLocalidad(superAdmin.getLocalidad());
        usuarioEntidad.setPartido(superAdmin.getPartido());
        usuarioEntidad.setProvincia(superAdmin.getProvincia());
        usuarioEntidad.setCuit(superAdmin.getCuit());
        usuarioEntidad.setTelefono(superAdmin.getTelefono());
        usuarioEntidad.setColegio(superAdmin.getColegio());


        Role adminRole = roleRepository.findByRol(RolesCostantes.SUPER_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RolesCostantes.SUPER_ADMIN)));
        usuarioEntidad.setRoles(Collections.singleton(adminRole));

        if (usuarioEntidad.getLogoInmobiliaria() != null) {
            usuarioEntidad.getLogoInmobiliaria().setNota(null);
            usuarioEntidad.getLogoInmobiliaria().setPropiedad(null);
        }

        Usuario usuarioPersistido = usuarioRepository.save(usuarioEntidad);

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
        dto.setColegio(usuario.getColegio());
        return dto;
    }

    @Override
    @Transactional
    public UsuarioDtoSalida buscarUsuarioPorNombreNegocio(String nombreNegocio) throws ResourceNotFoundException {
        Usuario usuario = usuarioRepository.findUserByNombreNegocio(nombreNegocio)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con inmobiliaria: " + nombreNegocio));

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
        dto.setColegio(usuario.getColegio());
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
    @Transactional // importante para que la sesión esté abierta mientras validás roles
    public AuthResponse loginUser(LoginEntradaDto loginEntradaDto) {

        String identifier = loginEntradaDto.username(); // puede ser email o nombreNegocio
        String password = loginEntradaDto.password();

        // 1️⃣ Buscar usuario con roles (y permisos si aplica)
        Usuario usuario = usuarioRepository.findByIdentifierWithRolesAndPerms(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2️⃣ Validar contraseña
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // 3️⃣ Bloquear roles propietarios e inquilinos
        boolean tieneRolRestringido = usuario.getRoles().stream()
                .map(role -> role.getRol().toUpperCase())
                .anyMatch(nombreRol -> nombreRol.equals("PROPIETARIO_USER") || nombreRol.equals("INQUILINO_USER"));

        if (tieneRolRestringido) {
            throw new AccessDeniedException("Este tipo de usuario no tiene acceso al panel principal.");
        }

        // 4️⃣ Autenticar y generar token
        Authentication authentication = this.authenticate(identifier, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Long userId = usuario.getId();
        String accessToken = jwtUtil.createAccessToken(authentication, userId);
        String refreshToken = jwtUtil.createRefreshToken(authentication);

        // 5️⃣ Responder
        return new AuthResponse(
                usuario.getUsername(), // ✅ devolvé el username real (interno)
                "Usuario logueado correctamente",
                accessToken,
                refreshToken,
                "Bearer",
                jwtUtil.getAccessTokenTtlSeconds(),
                true
        );
    }
    public UsuarioDtoSalida obtenerUsuarioPorIdDesdeToken(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UsuarioDtoSalida dto = modelMapper.map(usuario, UsuarioDtoSalida.class);

        // ✅ Asegurar que el logo sea la URL
        if (usuario.getLogoInmobiliaria() != null) {
            dto.setLogo(usuario.getLogoInmobiliaria().getImageUrl());
        } else {
            dto.setLogo(null);
        }

        return dto;
    }
    @Override
    public AuthResponse loginInquilino(LoginInquilinoEntradaDto loginEntradaDto) {

        String email = loginEntradaDto.getEmail();
        String password = loginEntradaDto.getPassword();

        Authentication authentication = this.authenticate(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Buscamos el ID del usuario para incluirlo en el JWT
        Long userId = usuarioRepository.findByEmail(email)
                .map(Usuario::getId)
                .orElse(null);

        String accessToken = jwtUtil.createAccessToken(authentication, userId);
        String refreshToken = jwtUtil.createRefreshToken(authentication);

        return new AuthResponse(
                email,
                "Inquilino logueado correctamente",
                accessToken,
                refreshToken,
                "Bearer",
                jwtUtil.getAccessTokenTtlSeconds(),
                true
        );
    }

    @Override
    public AuthResponse loginPropietario(LoginPropietarioEntradaDto loginEntradaDto) {

        String email = loginEntradaDto.getEmail();
        String password = loginEntradaDto.getPassword();

        Authentication authentication = this.authenticate(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Buscamos el ID del usuario para incluirlo en el JWT
        Long userId = usuarioRepository.findByEmail(email)
                .map(Usuario::getId)
                .orElse(null);

        String accessToken = jwtUtil.createAccessToken(authentication, userId);
        String refreshToken = jwtUtil.createRefreshToken(authentication);

        return new AuthResponse(
                email,
                "Propietario logueado correctamente",
                accessToken,
                refreshToken,
                "Bearer",
                jwtUtil.getAccessTokenTtlSeconds(),
                true
        );
    }





    public Authentication authenticate(String username, String password) {
        UserDetails userDetails = this.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException("usuario incorrecto o no registrado.");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("contraseña incorrecta.");
        }
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Usuario user = usuarioRepository.findByIdentifierWithRoles(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario no existe: " + identifier));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRol()))
        );
        user.getRoles().stream()
                .flatMap(role -> role.getPermisosList().stream())
                .forEach(perm -> authorities.add(new SimpleGrantedAuthority(perm.getName())));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }


    @Transactional
    @Override
    public UsuarioDtoSalida actualizarUsuario(Long id, ActualizarUsuarioDto dto) throws ResourceNotFoundException {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID " + id));

        // Actualizar los campos
        if (dto.getNombreNegocio() != null) usuario.setNombreNegocio(dto.getNombreNegocio());
        if (dto.getEmail() != null) {
            validarEmailUnicoEnActualizacion(dto.getEmail(), usuario.getId());
            usuario.setEmail(normalizarEmail(dto.getEmail()));
        }
        if (dto.getMatricula() != null) usuario.setMatricula(dto.getMatricula());
        if (dto.getRazonSocial() != null) usuario.setRazonSocial(dto.getRazonSocial());
        if (dto.getLocalidad() != null) usuario.setLocalidad(dto.getLocalidad());
        if (dto.getPartido() != null) usuario.setPartido(dto.getPartido());
        if (dto.getProvincia() != null) usuario.setProvincia(dto.getProvincia());
        if (dto.getCuit() != null) usuario.setCuit(dto.getCuit());
        if (dto.getTelefono() != null) usuario.setTelefono(dto.getTelefono());
        if (dto.getColegio() != null) usuario.setColegio(dto.getColegio());

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return modelMapper.map(usuarioActualizado, UsuarioDtoSalida.class);
    }


    @Override
    @Transactional
    public boolean deleteAccountByNombreNegocio(String nombreNegocio) {
        int deleted = usuarioRepository.deleteByNombreNegocio(nombreNegocio);
        LOGGER.info("deleteByNombreNegocio('{}') -> {} fila(s) borrada(s)", nombreNegocio, deleted);
        return deleted > 0;
    }

    private void validarEmailUnicoEnAlta(String email) {
        String emailNormalizado = normalizarEmail(email);
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new UsernameAlreadyExistsException("El email ya se encuentra registrado");
        }
    }

    private void validarEmailUnicoEnActualizacion(String email, Long userId) {
        String emailNormalizado = normalizarEmail(email);
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(emailNormalizado, userId)) {
            throw new UsernameAlreadyExistsException("El email ya se encuentra registrado");
        }
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }


    @Transactional
    public void guardarDatosCobroTransferencia(
            Long usuarioId,
            UsuarioCobroTransferenciaDto dto
    ) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // seguridad extra
        if (usuario.getInquilino() != null) {
            throw new RuntimeException("Un inquilino no puede configurar datos de cobro");
        }

        usuario.setMpAlias(dto.getAlias());
        usuario.setMpCbu(dto.getCbu());
        usuario.setMpTitular(dto.getTitular());
        usuario.setMpCuit(dto.getCuit());
        usuario.setMpBanco(dto.getBanco());

        usuarioRepository.save(usuario);
    }

    @Transactional
    public DatosCobroSoloUser listarDatosBancariosUser(Long userId) {
        return usuarioRepository.findDatosCobroById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    @Transactional
    public DatosCobroSoloUser editarDatosBancariosUser(Long userId, DatosCobroUpdateDto dto) {
        Usuario u = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Update parcial (solo lo que venga)
        if (dto.getAlias() != null) u.setMpAlias(dto.getAlias());
        if (dto.getCbu() != null) u.setMpCbu(dto.getCbu());
        if (dto.getTitular() != null) u.setMpTitular(dto.getTitular());
        if (dto.getCuit() != null) u.setMpCuit(dto.getCuit());
        if (dto.getBanco() != null) u.setMpBanco(dto.getBanco());

        usuarioRepository.save(u);

        // devolver lo actualizado (sin re-traer todo el user)
        return usuarioRepository.findDatosCobroById(userId)
                .orElseThrow(() -> new RuntimeException("No se pudieron obtener los datos actualizados"));
    }
}
