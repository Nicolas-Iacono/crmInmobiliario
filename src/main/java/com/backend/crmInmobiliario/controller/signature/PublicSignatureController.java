package com.backend.crmInmobiliario.controller.signature;

import com.backend.crmInmobiliario.DTO.entrada.signature.SignatureOtpDto;
import com.backend.crmInmobiliario.DTO.entrada.signature.SignatureSignDto;
import com.backend.crmInmobiliario.DTO.salida.signature.SignaturePublicAccessDto;
import com.backend.crmInmobiliario.service.signature.SignatureRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/public/signatures/access")
public class PublicSignatureController {

    private final SignatureRequestService signatureRequestService;

    public PublicSignatureController(SignatureRequestService signatureRequestService) {
        this.signatureRequestService = signatureRequestService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<SignaturePublicAccessDto> access(@PathVariable String token, HttpServletRequest request) {
        return ResponseEntity.ok(signatureRequestService.getAccess(token, request.getRemoteAddr(), request.getHeader("User-Agent")));
    }

    @PostMapping("/{token}/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@PathVariable String token, HttpServletRequest request) {
        signatureRequestService.sendOtp(token, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return ResponseEntity.ok(Map.of("message", "OTP enviado"));
    }

    @PostMapping("/{token}/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@PathVariable String token,
                                                          @Valid @RequestBody SignatureOtpDto dto,
                                                          HttpServletRequest request) {
        signatureRequestService.verifyOtp(token, dto, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return ResponseEntity.ok(Map.of("message", "OTP validado"));
    }

    @PostMapping("/{token}/sign")
    public ResponseEntity<Map<String, String>> sign(@PathVariable String token,
                                                     @Valid @RequestBody SignatureSignDto dto,
                                                     HttpServletRequest request) {
        signatureRequestService.sign(token, dto, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return ResponseEntity.ok(Map.of("message", "Firma registrada"));
    }
}
