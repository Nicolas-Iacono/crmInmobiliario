package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.AuthResponse;
import com.backend.crmInmobiliario.DTO.entrada.InquilinoEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.usuarioInquilino.LoginInquilinoEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.usuarioInquilino.RegistroInquilinoDto;
import com.backend.crmInmobiliario.DTO.modificacion.InquilinoDtoModificacion;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoUser;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioUser;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.IUserInquilinoService;
import com.backend.crmInmobiliario.service.IUsuarioService;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.service.impl.InquilinoService;
import com.backend.crmInmobiliario.service.impl.ReciboService;
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

@CrossOrigin(origins = "https://tuinmo.net")
@RestController
@AllArgsConstructor


@RequestMapping("/api/inquilino")
public class Inquilino {

    private final InquilinoService inquilinoService;
    private final ImagenService imagenService;
    private final IUserInquilinoService userInquilinoService;
    private final IUsuarioService userService;
    private final JwtUtil jwtUtil;
    private final ReciboService reciboService;
    private final AuthUtil authUtil;



    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InquilinoSalidaDto>> listarMisInquilinos() {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(inquilinoService.listarInquilinosPorUsuarioId(userId));
    }

    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    public ResponseEntity<TokenDtoSalida> registrar(@RequestBody RegistroInquilinoDto dto) {
        TokenDtoSalida response = userInquilinoService.registrarInquilino(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthResponse> login (@RequestBody LoginInquilinoEntradaDto loginEntradaDto){
        return new ResponseEntity<>(this.userService.loginInquilino(loginEntradaDto), HttpStatus.OK);
    }

    @GetMapping("/recibos")
    public ResponseEntity<List<ReciboSalidaDto>> obtenerRecibos(Authentication auth) {
        Long userId = jwtUtil.extractUserIdFromAuth(auth);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<ReciboSalidaDto> recibos = reciboService.obtenerPorInquilino(userId);
        return ResponseEntity.ok(recibos);
    }
    @GetMapping("/generar-embeddings")
    public ResponseEntity<?> generarEmbeddings() {
        try {
            Long userId = authUtil.extractUserId();
            inquilinoService.generarEmbeddingsParaUsuario(userId);
            return ResponseEntity.ok("✅ Embeddings generados correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
    @Transactional
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<InquilinoSalidaDto>> editarInquilino(@RequestBody InquilinoDtoModificacion inquilinoDtoModificacion) {
        try {
            InquilinoSalidaDto inquilinoSalidaDto = inquilinoService.editarInquilino(inquilinoDtoModificacion);
            return ResponseEntity.ok(new ApiResponse<>("Inquilino editado correctamente.", inquilinoSalidaDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("El inquilino no se encuentra en la DB", null));
        }
    }


    @Transactional
    @GetMapping("/enum/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Integer enumeraInquilinosr(@PathVariable String username) {
        Integer inquilinos = inquilinoService.enumerarInquilinos(username);
        return inquilinos;
    }
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<InquilinoSalidaDto>> crearInquilino(@Valid @RequestBody InquilinoEntradaDto inquilinoEntradaDto) {
        try {
            InquilinoSalidaDto inquilinoSalidaDto = inquilinoService.crearInquilino(inquilinoEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Inquilino creado correctamente.", inquilinoSalidaDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("El idCategory o idTheme no se encuentra en la DB", null));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<InquilinoSalidaDto>>> allInquilinos(){
        List<InquilinoSalidaDto> inquilinosSalidaDtos = inquilinoService.listarInquilinos();
        ApiResponse<List<InquilinoSalidaDto>> response =
                new ApiResponse<>("Lista de inquilinos: ", inquilinosSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarInquilino(@PathVariable Long id){
        try{
            inquilinoService.eliminarInquilino(id);
            return  ResponseEntity.ok(new ApiResponse<>("Inquilino con ID: " + id + " eliminado.", null ));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Inquilino no encontrado con el ID: " + id, null));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al eliminar el inquilino", null));
        }
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<ApiResponse<InquilinoSalidaDto>> buscarInquilinoPorId(@PathVariable Long id){
        try{
            InquilinoSalidaDto inquilinoBuscado = inquilinoService.buscarInquilinoPorId(id);
           return  ResponseEntity.ok(new ApiResponse<>("Inquilino encontrado, ", inquilinoBuscado));
        }catch (ResourceNotFoundException  e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("No se encontro el inquilino buscado, ", null));
        }
    }

    @GetMapping("/{username}")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<InquilinoSalidaDto>> getInquilinoByUsername(@PathVariable String username) {
        List<InquilinoSalidaDto> inquilinos = inquilinoService.buscarInquilinoPorUsuario(username);
        return ResponseEntity.ok(inquilinos);
    }


    @GetMapping("/credenciales/{inquilinoId}")
    public ResponseEntity<InquilinoUser> obtenerCredencialesPorInquilino(@PathVariable Long inquilinoId) {
        InquilinoUser credenciales = inquilinoService.listarCredenciales(inquilinoId);
        return ResponseEntity.ok(credenciales);

    }

//
//    @PostMapping("/{id}/imagenes")
//    public ResponseEntity<?> subirImagenesAInquilino(@PathVariable Long id,
//                                                     @RequestParam("files") MultipartFile[] archivos) {
//        try {
//            List<String> urls = imagenService.subirImagenesYAsociarAInquilino(id, archivos);
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

    @DeleteMapping("/usuario-inquilino/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> eliminarUsuarioInquilino(@PathVariable Long usuarioId) {
        inquilinoService.eliminarUsuarioCuentaInquilino(usuarioId);
        return ResponseEntity.ok("Usuario inquilino eliminado correctamente sin borrar al inquilino.");
    }

}
