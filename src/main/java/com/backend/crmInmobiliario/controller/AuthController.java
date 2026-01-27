package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.AuthResponse;
import com.backend.crmInmobiliario.DTO.entrada.LoginEntradaDto;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IUsuarioService;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.utils.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/usuario")
@CrossOrigin(origins = "https://tuinmo.net")
@PreAuthorize("denyAll()")
public class AuthController {
    private JwtUtil jwtUtil;
    private IUsuarioService userService;
    private ImagenService imagenService;
    private UsuarioRepository usuarioRepository;
    private ModelMapper modelMapper;
    public AuthController(ModelMapper modelMapper,JwtUtil jwtUtil,     IUsuarioService userService, ImagenService imagenService,UsuarioRepository usuarioRepository) {
        this.userService = userService;
        this.imagenService = imagenService;
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
    }


    @PostMapping("/login")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthResponse> login (@RequestBody LoginEntradaDto loginEntradaDto){
        return new ResponseEntity<>(this.userService.loginUser(loginEntradaDto), HttpStatus.OK);
    }
}
