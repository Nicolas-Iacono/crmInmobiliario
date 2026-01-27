package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.ResetPasswordDto;
import com.backend.crmInmobiliario.service.impl.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordResetService resetService;

    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        try {
            resetService.createAndSendToken(email);
        } catch (Exception e) {
            // intencionalmente no revelamos si falló
        }
        return ResponseEntity.ok(Map.of("message", "Si el correo existe, recibirás instrucciones para recuperar tu contraseña."));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody ResetPasswordDto dto) {
        try {
            resetService.resetPassword(dto.getEmail(), dto.getToken(), dto.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Tu contraseña fue actualizada correctamente."));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }
}
