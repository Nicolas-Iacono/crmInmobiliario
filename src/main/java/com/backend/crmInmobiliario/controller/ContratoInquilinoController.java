package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoBasicoDto;
import com.backend.crmInmobiliario.service.impl.ContratoService;
import com.backend.crmInmobiliario.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/inquilino/contrato")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ContratoInquilinoController {

    private final JwtUtil jwtUtil;

    @Autowired
    private ContratoService contratoService;

    // 🔹 El inquilino obtiene su contrato (solo lectura)
    @GetMapping("/mi-contrato")
    public ResponseEntity<ContratoBasicoDto> getContratoInquilino(HttpServletRequest request) {
        String username = jwtUtil.getUsernameFromRequest(request);

        ContratoBasicoDto contrato = contratoService.obtenerContratoBasicoPorInquilino(username);
        return ResponseEntity.ok(contrato);
    }
}
