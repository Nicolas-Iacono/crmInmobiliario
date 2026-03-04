package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.AuthResponse;
import com.backend.crmInmobiliario.DTO.entrada.PropietarioEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.usuarioPropietario.LoginPropietarioEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.usuarioPropietario.RegistroPropietarioDto;
import com.backend.crmInmobiliario.DTO.modificacion.PropietarioDtoModificacion;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.pages.PageResponse;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioUser;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.IUserPropietarioService;
import com.backend.crmInmobiliario.service.IUsuarioService;
import com.backend.crmInmobiliario.service.impl.*;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.backend.crmInmobiliario.utils.JwtUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la entidad Propietario
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/propietario")
@CrossOrigin(origins = "https://tuinmo.net")
public class PropietarioController {

    private final PropietarioService propietarioService;
    private final ImagenService imagenService;
    private final IUserPropietarioService userPropietarioService;
    private final IUsuarioService userService;
    private final JwtUtil jwtUtil;
    private final ReciboService reciboService;
    private final ContratoService contratoService;
    private final AuthUtil authUtil;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PropietarioSalidaDto>> listarMisPropietarios() {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(propietarioService.listarPropietariosPorUsuarioId(userId));
    }


    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    public ResponseEntity<TokenDtoSalida> registrar(@RequestBody RegistroPropietarioDto dto) {
        TokenDtoSalida response = userPropietarioService.registrarPropietario(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthResponse> login (@RequestBody LoginPropietarioEntradaDto loginEntradaDto){
        return new ResponseEntity<>(this.userService.loginPropietario(loginEntradaDto), HttpStatus.OK);
    }


    @GetMapping("/lista/page")
    public ResponseEntity<PageResponse<PropietarioSalidaDto>> listar(
            @RequestParam(defaultValue = "0") int page
    ) throws ResourceNotFoundException {
        return ResponseEntity.ok(propietarioService.listarPropietarios(page));
    }

    @Transactional
    @GetMapping("/enum")
    public Integer enumerar() {
        return propietarioService.enumerarPropietarios();
    }


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PropietarioSalidaDto>> crearPropietario(@Valid @RequestBody PropietarioEntradaDto PropietarioEntradaDto) {
        try {
            PropietarioSalidaDto propietarioSalidaDto = propietarioService.crearPropietario(PropietarioEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Propietario creado correctamente.", propietarioSalidaDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("El idCategory o idTheme no se encuentra en la DB", null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PropietarioSalidaDto>>> allPropietarios(){
        List<PropietarioSalidaDto> propietarioSalidaDtos = propietarioService.listarPropietarios();
        ApiResponse<List<PropietarioSalidaDto>> response =
                new ApiResponse<>("Lista de propietarios: ", propietarioSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarPropietario(@PathVariable Long id) {
        try {
            propietarioService.eliminarPropietario(id);
            return ResponseEntity.ok(
                    new ApiResponse<>("Propietario con ID: " + id + " eliminado.", null)
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Propietario no encontrado con el ID: " + id, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al eliminar el propietario", null));
        }
    }
    @GetMapping("/generar-embeddings")
    public ResponseEntity<?> generarEmbeddings() {
        try {
            Long userId = authUtil.extractUserId();
            propietarioService.generarEmbeddingsParaUsuario(userId);
            return ResponseEntity.ok("✅ Embeddings generados correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
    @GetMapping("/{username}")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PropietarioSalidaDto>> getPropietariosByUsername(@PathVariable String username) {
        List<PropietarioSalidaDto> propietarios =propietarioService.buscarPropietariosPorUsuario(username);
        return ResponseEntity.ok(propietarios);
    }
//
//    @PostMapping("/{id}/imagenes")
//    public ResponseEntity<?> subirImagenesAPropietario(@PathVariable Long id,
//                                                     @RequestParam("files") MultipartFile[] archivos) {
//        try {
//            List<String> urls = imagenService.subirImagenesYAsociarAPropietario(id, archivos);
//            return ResponseEntity.ok(urls);
//        } catch (Exception e) {
//            // 🔥 Imprimí el error para debug
//            e.printStackTrace();
//
//            // 🧠 Podés loguearlo con SLF4J si querés:
//            // log.error("Error al subir imágenes", e);
//
//            // 💬 Devolvés una respuesta clara al frontend
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error al subir las imágenes: " + e.getMessage());
//        }
//    }


    @Transactional
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @CrossOrigin(origins = "https://tuinmo.net")
    public ResponseEntity<ApiResponse<PropietarioSalidaDto>> editarPropietario(@RequestBody PropietarioDtoModificacion propietarioDtoModificacion) {
        try {
            PropietarioSalidaDto propietarioSalidaDto = propietarioService.editarPropietario(propietarioDtoModificacion);
            return ResponseEntity.ok(new ApiResponse<>("propietario editado correctamente.", propietarioSalidaDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("El propietario no se encuentra en la DB", null));
        }
    }
    @CrossOrigin(origins = "https://tuinmo.net")
    @GetMapping("/contratos/por-propietario")
    public ResponseEntity<List<ContratoSalidaDto>> obtenerContratosPorPropietario(Authentication auth) {
        String email = auth.getName(); // viene del JWT
        List<ContratoSalidaDto> contratos = contratoService.listarContratosPorPropietario(email);
        return ResponseEntity.ok(contratos);
    }

    @GetMapping("/credenciales/{propietarioId}")
    @CrossOrigin(origins = "https://tuinmo.net")
    public ResponseEntity<PropietarioUser> obtenerCredencialesPorPropietario(@PathVariable Long propietarioId) {
        PropietarioUser credenciales = propietarioService.listarCredenciales(propietarioId);
        return ResponseEntity.ok(credenciales);
    }

    @DeleteMapping("/usuario-propietario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> eliminarUsuarioPropietario(@PathVariable Long usuarioId) {
        propietarioService.eliminarUsuarioCuentaPropietario(usuarioId);
        return ResponseEntity.ok("Usuario propietario eliminado correctamente sin borrar al propietario.");
    }
}
