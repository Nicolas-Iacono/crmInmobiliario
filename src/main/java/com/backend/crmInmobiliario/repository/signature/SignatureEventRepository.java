package com.backend.crmInmobiliario.repository.signature;

import com.backend.crmInmobiliario.entity.signature.SignatureEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignatureEventRepository extends JpaRepository<SignatureEvent, Long> {
}
