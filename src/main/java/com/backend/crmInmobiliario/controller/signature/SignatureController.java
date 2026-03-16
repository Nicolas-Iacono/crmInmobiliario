package com.backend.crmInmobiliario.controller.signature;

import com.backend.crmInmobiliario.DTO.entrada.signature.CreateSignatureRequestDto;
import com.backend.crmInmobiliario.DTO.salida.signature.SignatureRequestResponseDto;
import com.backend.crmInmobiliario.service.signature.SignatureRequestService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/signatures")
public class SignatureController {

    private final SignatureRequestService signatureRequestService;
    private final AuthUtil authUtil;

    public SignatureController(SignatureRequestService signatureRequestService, AuthUtil authUtil) {
        this.signatureRequestService = signatureRequestService;
        this.authUtil = authUtil;
    }

    @PostMapping
    public ResponseEntity<SignatureRequestResponseDto> create(@Valid @RequestBody CreateSignatureRequestDto dto) {
        Long userId = authUtil.extractUserId();
        SignatureRequestResponseDto response = signatureRequestService.createSignatureRequest(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
