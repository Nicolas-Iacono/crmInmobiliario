package com.backend.crmInmobiliario.service.impl.IA;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodigoCivilManager {

    private final CodigoCivilExtractor extractor;
    private final CodigoCivilUploader uploader;

    public CodigoCivilManager(CodigoCivilExtractor extractor, CodigoCivilUploader uploader) {
        this.extractor = extractor;
        this.uploader = uploader;
    }

    public void procesarYSubir(String rutaPdf) throws Exception {
        System.out.println("📘 Extrayendo texto del Código Civil...");
        String texto = extractor.extraerTexto(rutaPdf);

        System.out.println("✂️ Dividiendo por artículos...");
        List<String> articulos = extractor.dividirPorArticulos(texto);

        System.out.println("🚀 Subiendo artículos a Supabase...");
        uploader.subirCodigoCivil(articulos);

        System.out.println("✅ Proceso completo: " + articulos.size() + " artículos subidos.");
    }
}
