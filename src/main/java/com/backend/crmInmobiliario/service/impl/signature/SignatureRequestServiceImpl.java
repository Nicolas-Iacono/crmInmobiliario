package com.backend.crmInmobiliario.service.impl.signature;

import com.backend.crmInmobiliario.DTO.entrada.signature.CreateSignatureRequestDto;
import com.backend.crmInmobiliario.DTO.entrada.signature.SignatureOtpDto;
import com.backend.crmInmobiliario.DTO.entrada.signature.SignatureSignDto;
import com.backend.crmInmobiliario.DTO.entrada.signature.SignatureSignerCreateDto;
import com.backend.crmInmobiliario.DTO.salida.signature.MySignaturePendingDto;
import com.backend.crmInmobiliario.DTO.salida.signature.SignaturePublicAccessDto;
import com.backend.crmInmobiliario.DTO.salida.signature.SignatureRequestResponseDto;
import com.backend.crmInmobiliario.DTO.salida.signature.SignatureSignerResponseDto;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.entity.signature.*;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.GaranteRepository;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.PropietarioRepository;
import com.backend.crmInmobiliario.repository.signature.SignatureEventRepository;
import com.backend.crmInmobiliario.repository.signature.SignatureRequestRepository;
import com.backend.crmInmobiliario.repository.signature.SignatureSignerRepository;
import com.backend.crmInmobiliario.service.signature.*;
import com.backend.crmInmobiliario.utils.signature.SignatureOtpUtil;
import com.backend.crmInmobiliario.utils.signature.SignatureSha256Util;
import com.backend.crmInmobiliario.utils.signature.SignatureTokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SignatureRequestServiceImpl implements SignatureRequestService {

    private final ContratoRepository contratoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SignatureRequestRepository signatureRequestRepository;
    private final SignatureSignerRepository signatureSignerRepository;
    private final SignatureEventRepository signatureEventRepository;
    private final SignatureSignerResolverService signatureSignerResolverService;
    private final ContractPdfProvider contractPdfProvider;
    private final SignatureMailService signatureMailService;
    private final SignatureStorageService signatureStorageService;
    private final SignatureNotificationService signatureNotificationService;
    private final PropietarioRepository propietarioRepository;
    private final InquilinoRepository inquilinoRepository;
    private final GaranteRepository garanteRepository;
    private final ObjectMapper objectMapper;

    public SignatureRequestServiceImpl(ContratoRepository contratoRepository,
                                       UsuarioRepository usuarioRepository,
                                       SignatureRequestRepository signatureRequestRepository,
                                       SignatureSignerRepository signatureSignerRepository,
                                       SignatureEventRepository signatureEventRepository,
                                       SignatureSignerResolverService signatureSignerResolverService,
                                       ContractPdfProvider contractPdfProvider,
                                       SignatureMailService signatureMailService,
                                       SignatureStorageService signatureStorageService,
                                       SignatureNotificationService signatureNotificationService,
                                       PropietarioRepository propietarioRepository,
                                       InquilinoRepository inquilinoRepository,
                                       GaranteRepository garanteRepository,
                                       ObjectMapper objectMapper) {
        this.contratoRepository = contratoRepository;
        this.usuarioRepository = usuarioRepository;
        this.signatureRequestRepository = signatureRequestRepository;
        this.signatureSignerRepository = signatureSignerRepository;
        this.signatureEventRepository = signatureEventRepository;
        this.signatureSignerResolverService = signatureSignerResolverService;
        this.contractPdfProvider = contractPdfProvider;
        this.signatureMailService = signatureMailService;
        this.signatureStorageService = signatureStorageService;
        this.signatureNotificationService = signatureNotificationService;
        this.propietarioRepository = propietarioRepository;
        this.inquilinoRepository = inquilinoRepository;
        this.garanteRepository = garanteRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MySignaturePendingDto> getMyPendingSignatures(Long userId) {
        List<SignatureSigner> signers = new ArrayList<>();

        propietarioRepository.findByUsuarioCuentaPropietarioId(userId)
                .ifPresent(owner -> signers.addAll(signatureSignerRepository.findBySignerRoleTypeAndRelatedEntityId(SignatureSignerRoleType.PROPIETARIO, owner.getId())));

        inquilinoRepository.findByUsuarioCuentaInquilinoId(userId)
                .ifPresent(tenant -> signers.addAll(signatureSignerRepository.findBySignerRoleTypeAndRelatedEntityId(SignatureSignerRoleType.INQUILINO, tenant.getId())));

        garanteRepository.findByUsuarioCuentaGaranteId(userId)
                .ifPresent(guarantor -> signers.addAll(signatureSignerRepository.findBySignerRoleTypeAndRelatedEntityId(SignatureSignerRoleType.GARANTE, guarantor.getId())));

        return signers.stream()
                .filter(this::isPendingForProfile)
                .sorted(Comparator.comparing((SignatureSigner s) -> s.getSignatureRequest().getExpiresAt()))
                .map(this::toMyPending)
                .toList();
    }

    @Override
    @Transactional
    public void sendOtpFromProfile(Long userId, Long signerId, String ip, String userAgent) {
        SignatureSigner signer = getSignerByIdForUserOrThrow(signerId, userId);
        sendOtp(signer.getAccessToken(), ip, userAgent);
    }

    @Override
    @Transactional
    public void verifyOtpFromProfile(Long userId, Long signerId, SignatureOtpDto dto, String ip, String userAgent) {
        SignatureSigner signer = getSignerByIdForUserOrThrow(signerId, userId);
        verifyOtp(signer.getAccessToken(), dto, ip, userAgent);
    }

    @Override
    @Transactional
    public void signFromProfile(Long userId, Long signerId, SignatureSignDto dto, String ip, String userAgent) {
        SignatureSigner signer = getSignerByIdForUserOrThrow(signerId, userId);
        sign(signer.getAccessToken(), dto, ip, userAgent);
    }

    @Override
    @Transactional
    public SignatureRequestResponseDto createSignatureRequest(CreateSignatureRequestDto dto, Long userId) {
        validateCreateDto(dto);

        Contrato contract = contratoRepository.findByIdAndUsuarioId(dto.getContractId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado o no pertenece al usuario autenticado"));

        Usuario createdBy = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));

        byte[] originalPdf = contractPdfProvider.getContractPdfBytes(contract.getId());
        String hash = SignatureSha256Util.sha256Hex(originalPdf);
        String originalPdfUrl = signatureStorageService.storeOriginalContractPdf(contract.getId(), originalPdf, hash);

        SignatureRequest request = new SignatureRequest();
        request.setContract(contract);
        request.setCreatedByUser(createdBy);
        request.setPdfHashSha256(hash);
        request.setPdfOriginalUrl(originalPdfUrl == null || originalPdfUrl.isBlank()
                ? contractPdfProvider.getContractPdfPublicUrl(contract.getId())
                : originalPdfUrl);
        request.setSequentialSigning(Boolean.TRUE.equals(dto.getSequentialSigning()));
        request.setStatus(SignatureRequestStatus.PENDING);
        request.setExpiresAt(LocalDateTime.now().plusHours(dto.getExpiresInHours()));

        Set<String> dedupe = new HashSet<>();
        List<Integer> orders = new ArrayList<>();

        int index = 1;
        for (SignatureSignerCreateDto signerDto : dto.getSigners()) {
            SignatureSignerRoleType role = parseRole(signerDto.getSignerRoleType());
            String uniqueKey = role.name() + ":" + signerDto.getRelatedEntityId();
            if (!dedupe.add(uniqueKey)) {
                throw new IllegalArgumentException("Firmante repetido: " + uniqueKey);
            }

            SignatureResolvedSignerData resolved = signatureSignerResolverService.resolveAndValidate(
                    contract,
                    role,
                    signerDto.getRelatedEntityId());

            Integer signOrder = signerDto.getSignOrder();
            if (request.isSequentialSigning()) {
                if (signOrder == null || signOrder <= 0) {
                    throw new IllegalArgumentException("En firma secuencial, signOrder es obligatorio y mayor a 0");
                }
                orders.add(signOrder);
            } else {
                signOrder = signOrder == null ? index : signOrder;
            }

            SignatureSigner signer = new SignatureSigner();
            signer.setContract(contract);
            signer.setSignatureRequest(request);
            signer.setSignerRoleType(resolved.getSignerRoleType());
            signer.setRelatedEntityId(resolved.getRelatedEntityId());
            signer.setFullName(resolved.getFullName());
            signer.setEmail(resolved.getEmail());
            signer.setPhone(resolved.getPhone());
            signer.setDni(resolved.getDni());
            signer.setSignOrder(signOrder);
            signer.setStatus(SignatureSignerStatus.PENDING);
            signer.setAccessToken(SignatureTokenUtil.generateUrlSafeToken());
            signer.setAccessTokenExpiresAt(request.getExpiresAt());
            request.getSigners().add(signer);
            index++;
        }

        if (request.isSequentialSigning()) {
            validateSequentialOrders(orders, dto.getSigners().size());
        }

        SignatureRequest saved = signatureRequestRepository.save(request);
        saved.getSigners().forEach(signatureNotificationService::notifySignerInvitation);
        recordEvent(saved, null, SignatureEventType.REQUEST_CREATED, null, null,
                Map.of("contractId", contract.getId(), "createdByUserId", createdBy.getId(), "signersCount", saved.getSigners().size()));

        return toResponse(saved);
    }

    @Override
    @Transactional
    public SignaturePublicAccessDto getAccess(String token, String ip, String userAgent) {
        SignatureSigner signer = getSignerByTokenOrThrow(token);
        validateTokenAndExpiration(signer);

        recordEvent(signer.getSignatureRequest(), signer, SignatureEventType.LINK_OPENED, ip, userAgent, null);
        return toPublicAccess(signer);
    }

    @Override
    @Transactional
    public void sendOtp(String token, String ip, String userAgent) {
        SignatureSigner signer = getSignerByTokenOrThrow(token);
        validateReadyToInteract(signer);
        validateSignerTurnIfSequential(signer);

        String otp = SignatureOtpUtil.generateSixDigits();
        signer.setOtpHash(SignatureSha256Util.sha256Hex(otp));
        signer.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        signer.setOtpValidatedAt(null);
        signer.setStatus(SignatureSignerStatus.OTP_SENT);

        signatureMailService.sendOtp(signer, otp);
        signatureSignerRepository.save(signer);
        recordEvent(signer.getSignatureRequest(), signer, SignatureEventType.OTP_SENT, ip, userAgent, null);
    }

    @Override
    @Transactional
    public void verifyOtp(String token, SignatureOtpDto dto, String ip, String userAgent) {
        SignatureSigner signer = getSignerByTokenOrThrow(token);
        validateReadyToInteract(signer);
        validateSignerTurnIfSequential(signer);

        if (signer.getOtpHash() == null || signer.getOtpExpiresAt() == null || signer.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP no generado o vencido");
        }

        String inputHash = SignatureSha256Util.sha256Hex(dto.getOtp());
        if (!inputHash.equals(signer.getOtpHash())) {
            recordEvent(signer.getSignatureRequest(), signer, SignatureEventType.OTP_FAILED, ip, userAgent, null);
            throw new IllegalArgumentException("OTP inválido");
        }

        signer.setOtpValidatedAt(LocalDateTime.now());
        signer.setStatus(SignatureSignerStatus.OTP_VERIFIED);
        signatureSignerRepository.save(signer);
        recordEvent(signer.getSignatureRequest(), signer, SignatureEventType.OTP_VERIFIED, ip, userAgent, null);
    }

    @Override
    @Transactional
    public void sign(String token, SignatureSignDto dto, String ip, String userAgent) {
        SignatureSigner signer = getSignerByTokenOrThrow(token);
        validateReadyToInteract(signer);
        validateSignerTurnIfSequential(signer);

        recordEvent(signer.getSignatureRequest(), signer, SignatureEventType.SIGN_ATTEMPT, ip, userAgent, null);

        if (!Boolean.TRUE.equals(dto.getConsentAccepted())) {
            throw new IllegalArgumentException("Debe aceptar el consentimiento para firmar");
        }

        if (signer.getOtpValidatedAt() == null) {
            throw new IllegalArgumentException("Debe validar OTP antes de firmar");
        }

        signer.setConsentAcceptedAt(LocalDateTime.now());
        signer.setSignedAt(LocalDateTime.now());
        signer.setIpAddress(ip);
        signer.setUserAgent(userAgent);
        signer.setSignatureDrawDataJson(dto.getSignatureDrawDataJson());
        signer.setSignatureImageBase64(dto.getSignatureImageBase64());
        signer.setStatus(SignatureSignerStatus.SIGNED);

        signer.setEvidenceJson(buildEvidence(signer, dto, ip, userAgent));
        signatureSignerRepository.save(signer);

        SignatureRequest request = signer.getSignatureRequest();
        updateRequestStatus(request);
        recordEvent(request, signer, SignatureEventType.CONSENT_ACCEPTED, ip, userAgent, Map.of("consentText", safe(dto.getConsentText())));
        recordEvent(request, signer, SignatureEventType.SIGNED, ip, userAgent, Map.of("signerId", signer.getId()));
        signatureNotificationService.notifyAgencySignerSigned(signer);
        signatureNotificationService.notifyAgencyRequestCompleted(signer);

        if (request.getStatus() == SignatureRequestStatus.SIGNED) {
            contractPdfProvider.saveFinalSignedPdf(request.getContract().getId(), request.getId(),
                    contractPdfProvider.getContractPdfBytes(request.getContract().getId()));
        }
    }

    private void validateCreateDto(CreateSignatureRequestDto dto) {
        if (dto.getExpiresInHours() == null || dto.getExpiresInHours() <= 0 || dto.getExpiresInHours() > 720) {
            throw new IllegalArgumentException("expiresInHours debe ser entre 1 y 720");
        }
        if (dto.getSigners() == null || dto.getSigners().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un firmante");
        }
    }

    private void validateSequentialOrders(List<Integer> orders, int expectedSize) {
        if (orders.size() != expectedSize) {
            throw new IllegalArgumentException("Todos los firmantes deben indicar signOrder");
        }
        Set<Integer> unique = new HashSet<>(orders);
        if (unique.size() != expectedSize) {
            throw new IllegalArgumentException("signOrder duplicado en firma secuencial");
        }
        for (int i = 1; i <= expectedSize; i++) {
            if (!unique.contains(i)) {
                throw new IllegalArgumentException("signOrder debe ser correlativo (1..n)");
            }
        }
    }

    private SignatureSignerRoleType parseRole(String rawRole) {
        try {
            return SignatureSignerRoleType.valueOf(rawRole.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException("signerRoleType inválido: " + rawRole);
        }
    }


    private SignatureSigner getSignerByIdForUserOrThrow(Long signerId, Long userId) {
        SignatureSigner signer = signatureSignerRepository.findById(signerId)
                .orElseThrow(() -> new IllegalArgumentException("Firmante no encontrado"));

        boolean belongs = switch (signer.getSignerRoleType()) {
            case PROPIETARIO -> propietarioRepository.findByUsuarioCuentaPropietarioId(userId)
                    .map(owner -> owner.getId().equals(signer.getRelatedEntityId()))
                    .orElse(false);
            case INQUILINO -> inquilinoRepository.findByUsuarioCuentaInquilinoId(userId)
                    .map(tenant -> tenant.getId().equals(signer.getRelatedEntityId()))
                    .orElse(false);
            case GARANTE -> garanteRepository.findByUsuarioCuentaGaranteId(userId)
                    .map(guarantor -> guarantor.getId().equals(signer.getRelatedEntityId()))
                    .orElse(false);
        };

        if (!belongs) {
            throw new IllegalArgumentException("No tenés permisos para operar este firmante");
        }
        return signer;
    }

    private SignatureSigner getSignerByTokenOrThrow(String token) {
        return signatureSignerRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de firma inválido"));
    }

    private void validateTokenAndExpiration(SignatureSigner signer) {
        SignatureRequest request = signer.getSignatureRequest();
        if (signer.getAccessTokenExpiresAt().isBefore(LocalDateTime.now())) {
            signer.setStatus(SignatureSignerStatus.EXPIRED);
            request.setStatus(SignatureRequestStatus.EXPIRED);
            throw new IllegalArgumentException("Token vencido");
        }
        if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
            request.setStatus(SignatureRequestStatus.EXPIRED);
            signer.setStatus(SignatureSignerStatus.EXPIRED);
            throw new IllegalArgumentException("Solicitud de firma vencida");
        }
        if (request.getStatus() == SignatureRequestStatus.CANCELLED || request.getStatus() == SignatureRequestStatus.REJECTED) {
            throw new IllegalArgumentException("Solicitud no disponible para firma");
        }
    }

    private void validateReadyToInteract(SignatureSigner signer) {
        validateTokenAndExpiration(signer);
        if (signer.getStatus() == SignatureSignerStatus.SIGNED) {
            throw new IllegalArgumentException("Este firmante ya firmó");
        }
        if (signer.getStatus() == SignatureSignerStatus.REJECTED || signer.getStatus() == SignatureSignerStatus.EXPIRED) {
            throw new IllegalArgumentException("Firmante no habilitado");
        }
    }

    private void validateSignerTurnIfSequential(SignatureSigner signer) {
        SignatureRequest request = signer.getSignatureRequest();
        if (!request.isSequentialSigning()) {
            return;
        }

        List<SignatureSigner> signers = signatureSignerRepository.findBySignatureRequestIdOrderBySignOrderAscIdAsc(request.getId());
        for (SignatureSigner current : signers) {
            if (current.getStatus() != SignatureSignerStatus.SIGNED) {
                if (!current.getId().equals(signer.getId())) {
                    throw new IllegalArgumentException("Firma secuencial: todavía no es el turno de este firmante");
                }
                return;
            }
        }
    }

    private void updateRequestStatus(SignatureRequest request) {
        List<SignatureSigner> signers = signatureSignerRepository.findBySignatureRequestIdOrderBySignOrderAscIdAsc(request.getId());
        boolean allSigned = signers.stream().allMatch(s -> s.getStatus() == SignatureSignerStatus.SIGNED);

        if (allSigned) {
            request.setStatus(SignatureRequestStatus.SIGNED);
        } else {
            request.setStatus(SignatureRequestStatus.PARTIALLY_SIGNED);
        }
        signatureRequestRepository.save(request);
    }

    private void recordEvent(SignatureRequest request,
                             SignatureSigner signer,
                             SignatureEventType eventType,
                             String ip,
                             String userAgent,
                             Map<String, Object> metadata) {
        SignatureEvent event = new SignatureEvent();
        event.setSignatureRequest(request);
        event.setSigner(signer);
        event.setEventType(eventType);
        event.setIpAddress(ip);
        event.setUserAgent(userAgent);
        if (metadata != null && !metadata.isEmpty()) {
            try {
                event.setMetadataJson(objectMapper.writeValueAsString(metadata));
            } catch (JsonProcessingException e) {
                event.setMetadataJson("{}");
            }
        }
        signatureEventRepository.save(event);
    }

    private String buildEvidence(SignatureSigner signer, SignatureSignDto dto, String ip, String userAgent) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        SignatureRequest request = signer.getSignatureRequest();

        evidence.put("signatureRequestId", request.getId());
        evidence.put("contractId", request.getContract().getId());
        evidence.put("documentHashSha256", request.getPdfHashSha256());
        evidence.put("signerEmail", signer.getEmail());
        evidence.put("signedAt", signer.getSignedAt());
        evidence.put("ip", ip);
        evidence.put("userAgent", userAgent);
        evidence.put("consentAccepted", dto.getConsentAccepted());
        evidence.put("consentText", safe(dto.getConsentText()));
        evidence.put("otpValidated", signer.getOtpValidatedAt() != null);
        evidence.put("signerRoleType", signer.getSignerRoleType().name());
        evidence.put("relatedEntityId", signer.getRelatedEntityId());
        evidence.put("signatureDrawDataJson", safe(dto.getSignatureDrawDataJson()));
        evidence.put("signatureImageBase64", safe(dto.getSignatureImageBase64()));

        try {
            return objectMapper.writeValueAsString(evidence);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar evidencia de firma", e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }


    private boolean isPendingForProfile(SignatureSigner signer) {
        SignatureRequest request = signer.getSignatureRequest();
        if (request == null || request.getContract() == null) {
            return false;
        }
        if (request.getStatus() == SignatureRequestStatus.SIGNED
                || request.getStatus() == SignatureRequestStatus.CANCELLED
                || request.getStatus() == SignatureRequestStatus.REJECTED
                || request.getStatus() == SignatureRequestStatus.EXPIRED) {
            return false;
        }
        if (signer.getStatus() == SignatureSignerStatus.SIGNED
                || signer.getStatus() == SignatureSignerStatus.REJECTED
                || signer.getStatus() == SignatureSignerStatus.EXPIRED) {
            return false;
        }
        if (signer.getAccessTokenExpiresAt() == null || signer.getAccessTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        return request.getExpiresAt() != null && !request.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private MySignaturePendingDto toMyPending(SignatureSigner signer) {
        SignatureRequest request = signer.getSignatureRequest();
        return MySignaturePendingDto.builder()
                .signerId(signer.getId())
                .accessToken(signer.getAccessToken())
                .requestId(request.getId())
                .contractId(request.getContract().getId())
                .contractName(request.getContract().getNombreContrato())
                .signerRoleType(signer.getSignerRoleType())
                .signerStatus(signer.getStatus())
                .requestStatus(request.getStatus())
                .enabledToSignNow(isEnabledToSignNow(signer))
                .requestExpiresAt(request.getExpiresAt())
                .build();
    }

    private SignatureRequestResponseDto toResponse(SignatureRequest request) {
        List<SignatureSignerResponseDto> signerDtos = request.getSigners().stream()
                .map(this::toSignerResponse)
                .toList();

        return SignatureRequestResponseDto.builder()
                .id(request.getId())
                .contractId(request.getContract().getId())
                .createdByUserId(request.getCreatedByUser().getId())
                .pdfHashSha256(request.getPdfHashSha256())
                .pdfOriginalUrl(request.getPdfOriginalUrl())
                .status(request.getStatus())
                .sequentialSigning(request.isSequentialSigning())
                .expiresAt(request.getExpiresAt())
                .signers(signerDtos)
                .build();
    }

    private SignatureSignerResponseDto toSignerResponse(SignatureSigner signer) {
        return SignatureSignerResponseDto.builder()
                .id(signer.getId())
                .signerRoleType(signer.getSignerRoleType())
                .relatedEntityId(signer.getRelatedEntityId())
                .fullName(signer.getFullName())
                .email(signer.getEmail())
                .phone(signer.getPhone())
                .dni(signer.getDni())
                .signOrder(signer.getSignOrder())
                .status(signer.getStatus())
                .signedAt(signer.getSignedAt())
                .build();
    }

    private SignaturePublicAccessDto toPublicAccess(SignatureSigner signer) {
        SignatureRequest request = signer.getSignatureRequest();
        return SignaturePublicAccessDto.builder()
                .requestId(request.getId())
                .contractId(request.getContract().getId())
                .requestStatus(request.getStatus())
                .signerStatus(signer.getStatus())
                .signerRoleType(signer.getSignerRoleType())
                .relatedEntityId(signer.getRelatedEntityId())
                .fullName(signer.getFullName())
                .email(signer.getEmail())
                .sequentialSigning(request.isSequentialSigning())
                .signOrder(signer.getSignOrder())
                .enabledToSignNow(isEnabledToSignNow(signer))
                .requestExpiresAt(request.getExpiresAt())
                .accessTokenExpiresAt(signer.getAccessTokenExpiresAt())
                .pdfOriginalUrl(request.getPdfOriginalUrl())
                .build();
    }

    private boolean isEnabledToSignNow(SignatureSigner signer) {
        try {
            validateReadyToInteract(signer);
            validateSignerTurnIfSequential(signer);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
