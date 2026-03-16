package com.backend.crmInmobiliario.service.impl.signature;

import com.backend.crmInmobiliario.service.signature.SignatureStorageService;
import org.springframework.stereotype.Service;

@Service
public class DefaultSignatureStorageService implements SignatureStorageService {

    @Override
    public String storeOriginalContractPdf(Long contractId, byte[] pdfBytes, String sha256Hash) {
        // Adaptar para guardar en S3/Supabase/otro storage y devolver URL pública.
        return "internal://contracts/" + contractId + "/signature/source/" + sha256Hash + ".pdf";
    }
}
