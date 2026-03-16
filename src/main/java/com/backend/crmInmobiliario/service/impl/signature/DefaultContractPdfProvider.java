package com.backend.crmInmobiliario.service.impl.signature;

import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.service.signature.ContractPdfProvider;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class DefaultContractPdfProvider implements ContractPdfProvider {

    private final ContratoRepository contratoRepository;

    public DefaultContractPdfProvider(ContratoRepository contratoRepository) {
        this.contratoRepository = contratoRepository;
    }

    @Override
    public byte[] getContractPdfBytes(Long contractId) {
        Contrato contrato = contratoRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado para PDF"));

        // Adaptar: reemplazar por generación/obtención real de PDF binario.
        String raw = contrato.getPdfContratoTexto() == null ? "" : contrato.getPdfContratoTexto();
        return raw.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getContractPdfPublicUrl(Long contractId) {
        return "internal://contracts/" + contractId + "/pdf/original";
    }

    @Override
    public void saveFinalSignedPdf(Long contractId, Long signatureRequestId, byte[] finalPdfBytes) {
        // Adaptar: persistir PDF final firmado si tu implementación lo requiere.
    }
}
