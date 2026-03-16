package com.backend.crmInmobiliario.service.signature;

public interface ContractPdfProvider {
    byte[] getContractPdfBytes(Long contractId);

    String getContractPdfPublicUrl(Long contractId);

    void saveFinalSignedPdf(Long contractId, Long signatureRequestId, byte[] finalPdfBytes);
}
