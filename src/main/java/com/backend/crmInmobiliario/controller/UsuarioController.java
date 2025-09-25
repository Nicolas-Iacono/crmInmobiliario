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
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IUsuarioService;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public UsuarioController(JwtUtil jwtUtil,     IUsuarioService userService, ImagenService imagenService,UsuarioRepository usuarioRepository) {
        this.userService = userService;
        this.imagenService = imagenService;
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
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

    @GetMapping("/username/{username}")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> obtenerUsuarioPorUsername(@PathVariable String username) {
        try {
            UsuarioDtoSalida usuario = userService.buscarUsuarioPorUsername(username);
            return ResponseEntity.ok(usuario);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Usuario no encontrado con username: " + username, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error inesperado", null));
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteByUsername(@PathVariable String username) {
        userService.deleteAccountByUsername(username);
        return ResponseEntity.noContent().build();
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
            String authorities = ud.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            String newAccess = jwtUtil.createAccessToken(username, authorities);

            return ResponseEntity.ok(new RefreshResponse(newAccess, null, "Bearer", 15 * 60));
        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("refresh_token_expired"));
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("refresh_token_invalid"));
        }
    }


}
