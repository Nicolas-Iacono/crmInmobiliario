package com.backend.crmInmobiliario.controller.signature;

import com.backend.crmInmobiliario.DTO.entrada.signature.CreateSignatureRequestDto;
import com.backend.crmInmobiliario.DTO.entrada.signature.SignatureOtpDto;
import com.backend.crmInmobiliario.DTO.entrada.signature.SignatureSignDto;
import com.backend.crmInmobiliario.DTO.salida.signature.MySignaturePendingDto;
import com.backend.crmInmobiliario.DTO.salida.signature.SignatureRequestResponseDto;
import com.backend.crmInmobiliario.service.signature.SignatureRequestService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/signatures")
public class SignatureController {

    private final SignatureRequestService signatureRequestService;
    private final AuthUtil authUtil;

    public SignatureController(SignatureRequestService signatureRequestService, AuthUtil authUtil) {
        this.signatureRequestService = signatureRequestService;
        this.authUtil = authUtil;
    }

    @GetMapping("/me/pending")
    public ResponseEntity<List<MySignaturePendingDto>> getMyPendingSignatures() {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(signatureRequestService.getMyPendingSignatures(userId));
    }

    @PostMapping("/me/{signerId}/send-otp")
    public ResponseEntity<Map<String, String>> sendOtpFromProfile(@PathVariable Long signerId, HttpServletRequest request) {
        Long userId = authUtil.extractUserId();
        signatureRequestService.sendOtpFromProfile(userId, signerId, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return ResponseEntity.ok(Map.of("message", "OTP enviado"));
    }

    @PostMapping("/me/{signerId}/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtpFromProfile(@PathVariable Long signerId,
                                                                     @Valid @RequestBody SignatureOtpDto dto,
                                                                     HttpServletRequest request) {
        Long userId = authUtil.extractUserId();
        signatureRequestService.verifyOtpFromProfile(userId, signerId, dto, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return ResponseEntity.ok(Map.of("message", "OTP validado"));
    }

    @PostMapping("/me/{signerId}/sign")
    public ResponseEntity<Map<String, String>> signFromProfile(@PathVariable Long signerId,
                                                                @Valid @RequestBody SignatureSignDto dto,
                                                                HttpServletRequest request) {
        Long userId = authUtil.extractUserId();
        signatureRequestService.signFromProfile(userId, signerId, dto, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return ResponseEntity.ok(Map.of("message", "Firma registrada"));
    }

    @PostMapping
    public ResponseEntity<SignatureRequestResponseDto> create(@Valid @RequestBody CreateSignatureRequestDto dto) {
        Long userId = authUtil.extractUserId();
        SignatureRequestResponseDto response = signatureRequestService.createSignatureRequest(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
