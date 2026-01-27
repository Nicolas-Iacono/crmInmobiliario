package com.backend.crmInmobiliario.service.impl.IA;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CodigoCivilExtractor {

    public String extraerTexto(String rutaPdf) throws IOException {
        try (PDDocument document = PDDocument.load(new File(rutaPdf))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    public List<String> dividirPorArticulos(String texto) {
        List<String> articulos = new ArrayList<>();
        String[] partes = texto.split("(?=Artículo\\s+[0-9]+\\.)"); // separa por "Artículo 1."
        for (String parte : partes) {
            String limpio = parte.trim();
            if (!limpio.isEmpty()) articulos.add(limpio);
        }
        return articulos;
    }
}
