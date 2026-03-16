package com.backend.crmInmobiliario.repository.signature;

import com.backend.crmInmobiliario.entity.signature.SignatureSigner;
import com.backend.crmInmobiliario.entity.signature.SignatureSignerRoleType;
import com.backend.crmInmobiliario.entity.signature.SignatureSignerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SignatureSignerRepository extends JpaRepository<SignatureSigner, Long> {

    Optional<SignatureSigner> findByAccessToken(String accessToken);

    boolean existsBySignatureRequestIdAndSignerRoleTypeAndRelatedEntityId(Long signatureRequestId,
                                                                           SignatureSignerRoleType signerRoleType,
                                                                           Long relatedEntityId);

    List<SignatureSigner> findBySignatureRequestIdOrderBySignOrderAscIdAsc(Long signatureRequestId);

    long countBySignatureRequestIdAndStatus(Long signatureRequestId, SignatureSignerStatus status);
}
