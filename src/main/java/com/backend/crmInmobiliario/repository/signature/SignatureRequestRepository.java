package com.backend.crmInmobiliario.repository.signature;

import com.backend.crmInmobiliario.entity.signature.SignatureRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignatureRequestRepository extends JpaRepository<SignatureRequest, Long> {

    @EntityGraph(attributePaths = {"signers", "contract", "createdByUser"})
    Optional<SignatureRequest> findById(Long id);
}
