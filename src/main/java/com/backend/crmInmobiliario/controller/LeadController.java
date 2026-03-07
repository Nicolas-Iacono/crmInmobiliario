package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.entity.Lead;
import com.backend.crmInmobiliario.repository.LeadRepository;
import com.backend.crmInmobiliario.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadRepository leadRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/mis-leads")
    public ResponseEntity<List<Lead>> myLeads(Authentication authentication) {
        Long usuarioId = jwtUtil.extractUserIdFromAuth(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(leadRepository.findByUsuarioId(usuarioId));
    }
}
