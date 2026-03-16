package com.backend.crmInmobiliario.service.signature;

import com.backend.crmInmobiliario.DTO.entrada.signature.CreateSignatureRequestDto;
import com.backend.crmInmobiliario.DTO.entrada.signature.SignatureOtpDto;
import com.backend.crmInmobiliario.DTO.entrada.signature.SignatureSignDto;
import com.backend.crmInmobiliario.DTO.salida.signature.SignaturePublicAccessDto;
import com.backend.crmInmobiliario.DTO.salida.signature.SignatureRequestResponseDto;

public interface SignatureRequestService {
    SignatureRequestResponseDto createSignatureRequest(CreateSignatureRequestDto dto, Long userId);

    SignaturePublicAccessDto getAccess(String token, String ip, String userAgent);

    void sendOtp(String token, String ip, String userAgent);

    void verifyOtp(String token, SignatureOtpDto dto, String ip, String userAgent);

    void sign(String token, SignatureSignDto dto, String ip, String userAgent);
}
