package com.backend.crmInmobiliario.service.signature;

public interface SignatureStorageService {
    String storeOriginalContractPdf(Long contractId, byte[] pdfBytes, String sha256Hash);
}
