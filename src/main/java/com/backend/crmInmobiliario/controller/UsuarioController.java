package com.backend.crmInmobiliario.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.crmInmobiliario.DTO.AuthResponse;
import com.backend.crmInmobiliario.DTO.entrada.LoginEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.UserAdminEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.jwt.ErrorResponse;
import com.backend.crmInmobiliario.DTO.entrada.jwt.RefreshRequest;
import com.backend.crmInmobiliario.DTO.entrada.jwt.RefreshResponse;
import com.backend.crmInmobiliario.DTO.modificacion.ActualizarUsuarioDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.entrada.UsuarioCobroTransferenciaDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.modificacion.DatosCobroUpdateDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.salida.DatosCobroSoloUser;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.salida.UsuarioCobroTransferenciaSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IUsuarioService;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.backend.crmInmobiliario.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuario")
@CrossOrigin(origins = "https://tuinmo.net")
@PreAuthorize("denyAll()")
public class UsuarioController {
    private JwtUtil jwtUtil;
    private IUsuarioService userService;
    private ImagenService imagenService;
    private UsuarioRepository usuarioRepository;
    private ModelMapper modelMapper;
    private AuthUtil authUtil;
    public UsuarioController( AuthUtil authUtil, ModelMapper modelMapper,JwtUtil jwtUtil,     IUsuarioService userService, ImagenService imagenService,UsuarioRepository usuarioRepository) {
        this.userService = userService;
        this.imagenService = imagenService;
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
    }

    @PreAuthorize("permitAll()")
    @CrossOrigin(origins = "https://tuinmo.net")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UsuarioDtoSalida>>> allUsuarios(){
        List<UsuarioDtoSalida> usuariosSalidaDtos = userService.listarUsuarios();
        ApiResponse<List<UsuarioDtoSalida>> response =
                new ApiResponse<>("Lista de usuarios: ", usuariosSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/registrar")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TokenDtoSalida> registrarUsuario(@RequestBody @Valid UserAdminEntradaDto userAdminEntradaDto){

        return new ResponseEntity<>(userService.registrarUsuario(userAdminEntradaDto), HttpStatus.CREATED);
    }

    @PostMapping("/registrar-super-admin")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TokenDtoSalida> registrarSuperAdmin(@RequestBody @Valid UserAdminEntradaDto userAdminEntradaDto){

        return new ResponseEntity<>(userService.registrarUsuarioSuperAdmin(userAdminEntradaDto), HttpStatus.CREATED);
    }


    @PostMapping("/registrar-admin")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TokenDtoSalida> registrarAdmin(@RequestBody @Valid UserAdminEntradaDto userAdminEntradaDto){

        return new ResponseEntity<>(userService.registrarUsuarioAdmin(userAdminEntradaDto), HttpStatus.CREATED);
    }
    @PreAuthorize("permitAll()")
    @CrossOrigin(origins = {"http://localhost:3000", "https://darkgreen-ferret-296866.hostingersite.com","https://tuinmo.net"})
    @GetMapping("/check-username")
    public Map<String, Boolean> checkUsername(@RequestParam String username) {
        boolean exists = usuarioRepository.existsByUsername(username);
        return Map.of("available", !exists);
    }

    @PostMapping("/login")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthResponse> login (@RequestBody LoginEntradaDto loginEntradaDto){
        return new ResponseEntity<>(this.userService.loginUser(loginEntradaDto), HttpStatus.OK);
    }
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDtoSalida> obtenerMiUsuario(HttpServletRequest request) {


        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);

        // ✅ Validar y decodificar token
        DecodedJWT decodedJWT = jwtUtil.validateAccessToken(token);

        // ✅ Extraer userId del JWT
        Long userId = jwtUtil.extractUserId(decodedJWT);

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UsuarioDtoSalida dto = modelMapper.map(usuario, UsuarioDtoSalida.class);

        // ✅ Ajuste extra: devolver la URL del logo correctamente
        if (usuario.getLogoInmobiliaria() != null) {
            dto.setLogo(usuario.getLogoInmobiliaria().getImageUrl());
        }

        return ResponseEntity.ok(dto);
    }


    @CrossOrigin(origins = "https://tuinmo.net")
    @PostMapping("/{id}/logo")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> subirLogo(@PathVariable Long id,
                                                     @RequestParam("file") MultipartFile archivo) {
        try {
            ImgUrlSalidaDto imagenes = imagenService.subirLogo(id, archivo);
            return ResponseEntity.ok(imagenes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir las imágenes: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/logo")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> eliminarLogo(@PathVariable Long id) {
        try {
            imagenService.eliminarLogo(id);
            return ResponseEntity.ok("Logo eliminado correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el logo: " + e.getMessage());
        }
    }

    @GetMapping("/nombre-negocio/{nombreNegocio}")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> obtenerUsuarioPorNombreNegocio(@PathVariable String nombreNegocio) {
        try {
            UsuarioDtoSalida usuario = userService.buscarUsuarioPorNombreNegocio(nombreNegocio);
            return ResponseEntity.ok(usuario);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Usuario no encontrado con nombreNegocio: " + nombreNegocio, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error inesperado", null));
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody ActualizarUsuarioDto dto) {
        try {
            UsuarioDtoSalida actualizado = userService.actualizarUsuario(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Usuario no encontrado", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error al actualizar el usuario", null));
        }
    }

    @PreAuthorize("#username == authentication.name or hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{nombreNegocio}")
    public ResponseEntity<?> deleteByNombreNegocio(@PathVariable String nombreNegocio) {
        boolean deleted = userService.deleteAccountByNombreNegocio(nombreNegocio);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        return ResponseEntity.noContent().build(); // 204
    }
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest req) {
        try {
            DecodedJWT jwt = jwtUtil.validateRefreshToken(req.getRefreshToken());
            String username = jwt.getSubject();

            // Cargamos authorities frescos
            UserDetails ud = userService.loadUserByUsername(username);
            Long userId = usuarioRepository.findByUsername(username)
                    .map(Usuario::getId)
                    .orElse(null);

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    ud, null, ud.getAuthorities()
            );

            String newAccess = jwtUtil.createAccessToken(auth, userId);
            String newRefresh = jwtUtil.createRefreshToken(auth);

            return ResponseEntity.ok(new RefreshResponse(newAccess, newRefresh, "Bearer", jwtUtil.getAccessTokenTtlSeconds()));
        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("refresh_token_expired"));
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("refresh_token_invalid"));
        }
    }

    @PostMapping("/cobro/transferencia")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> guardarDatosCobro(
            @Valid @RequestBody UsuarioCobroTransferenciaDto dto
    ) {
        Long userId = authUtil.extractUserId();
        userService.guardarDatosCobroTransferencia(userId, dto);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/cobro/{usuarioId}/transferencia")
    public UsuarioCobroTransferenciaSalidaDto obtenerDatosCobro(@PathVariable Long usuarioId) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UsuarioCobroTransferenciaSalidaDto dto = new UsuarioCobroTransferenciaSalidaDto();
        dto.setAlias(u.getMpAlias());
        dto.setCbu(u.getMpCbu());
        dto.setTitular(u.getMpTitular());
        dto.setBanco(u.getMpBanco());
        return dto;
    }


    // 🔹 Obtener datos bancarios del usuario logueado
    @GetMapping("/me/datosmp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DatosCobroSoloUser> listar() {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(userService.listarDatosBancariosUser(userId));
    }

    // 🔹 Editar datos bancarios del usuario logueado
    @PutMapping("/me/datosmp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DatosCobroSoloUser> editar(
            @Valid @RequestBody DatosCobroUpdateDto dto
    ) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(userService.editarDatosBancariosUser(userId, dto));
    }
}
