package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.AuthResponse;
import com.backend.crmInmobiliario.DTO.entrada.LoginEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.UserAdminEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.IUsuarioService;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/usuario")
@CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
@PreAuthorize("denyAll()")
public class UsuarioController {
    private IUsuarioService userService;
    private ImagenService imagenService;
    public UsuarioController(IUsuarioService userService, ImagenService imagenService) {
        this.userService = userService;
        this.imagenService = imagenService;
    }

    @PreAuthorize("permitAll()")
    @CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UsuarioDtoSalida>>> allUsuarios(){
        List<UsuarioDtoSalida> usuariosSalidaDtos = userService.listarUsuarios();
        ApiResponse<List<UsuarioDtoSalida>> response =
                new ApiResponse<>("Lista de usuarios: ", usuariosSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("/registrar-admin")
    @CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TokenDtoSalida> registrarAdmin(@RequestBody @Valid UserAdminEntradaDto userAdminEntradaDto){

        return new ResponseEntity<>(userService.registrarUsuarioAdmin(userAdminEntradaDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthResponse> login (@RequestBody LoginEntradaDto loginEntradaDto){
        return new ResponseEntity<>(this.userService.loginUser(loginEntradaDto), HttpStatus.OK);
    }

    @CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
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
    @CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
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
    @CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
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
}
