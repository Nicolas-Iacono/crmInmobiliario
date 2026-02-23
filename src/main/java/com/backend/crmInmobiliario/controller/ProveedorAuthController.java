package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.LoginEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.aliados.ProveedorLoginRequest;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.impl.UserService;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proveedores/auth")
@RequiredArgsConstructor
public class ProveedorAuthController {

    private final UserService authService; // el que uses para loguear
    private final UsuarioRepository usuarioRepository;


    @PostMapping("/login")
    @PermitAll
    public ResponseEntity<?> loginProveedor(@RequestBody LoginEntradaDto req) {

        var usuarioOpt = usuarioRepository.findByIdentifierWithRolesAndPerms(req.username());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }

        var usuario = usuarioOpt.get();

        boolean esProveedor = usuario.getRoles().stream()
                .map(r -> r.getRol().toUpperCase())
                .anyMatch(rol -> rol.equals(RolesCostantes.OFICIO_ADMIN));

        if (!esProveedor) { // ✅ al revés
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Este login es solo para proveedores");
        }

        return ResponseEntity.ok(authService.loginUser(req)); // ✅ usa el mismo login
    }
}
