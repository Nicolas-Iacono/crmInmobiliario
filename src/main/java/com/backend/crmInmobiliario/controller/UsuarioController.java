package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.AuthResponse;
import com.backend.crmInmobiliario.DTO.entrada.LoginEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.UserAdminEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.service.IUsuarioService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuario")
@CrossOrigin
@PreAuthorize("denyAll()")
public class UsuarioController {
    private IUsuarioService userService;

    public UsuarioController(IUsuarioService userService) {
        this.userService = userService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("permitAll()")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UsuarioDtoSalida>>> allUsuarios(){
        List<UsuarioDtoSalida> usuariosSalidaDtos = userService.listarUsuarios();
        ApiResponse<List<UsuarioDtoSalida>> response =
                new ApiResponse<>("Lista de usuarios: ", usuariosSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("/registrar-admin")
    @PreAuthorize("permitAll()")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<TokenDtoSalida> registrarAdmin(@RequestBody @Valid UserAdminEntradaDto userAdminEntradaDto){

        return new ResponseEntity<>(userService.registrarUsuarioAdmin(userAdminEntradaDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<AuthResponse> login (@RequestBody LoginEntradaDto loginEntradaDto){
        return new ResponseEntity<>(this.userService.loginUser(loginEntradaDto), HttpStatus.OK);
    }
}
