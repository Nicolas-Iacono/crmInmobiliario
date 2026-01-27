package com.backend.crmInmobiliario.service.impl.IA;


import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class CodigoCivilUploader {

    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.service.role.key}")
    private String SUPABASE_SERVICE_ROLE_KEY;

    private final EmbeddingService embeddingService;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public CodigoCivilUploader(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * ✅ Modo simple: lee un archivo TXT (ya extraído del PDF) y lo divide por artículos
     */
    public void subirCodigo(Path archivo) throws Exception {
        List<String> lineas = Files.readAllLines(archivo);
        StringBuilder buffer = new StringBuilder();
        String articuloActual = null;
        int total = 0;

        for (String linea : lineas) {
            if (linea.startsWith("Artículo")) {
                if (articuloActual != null) {
                    guardarArticulo(articuloActual, buffer.toString(), ++total);
                }
                articuloActual = linea.trim();
                buffer = new StringBuilder();
            } else {
                buffer.append(linea).append("\n");
            }
        }

        if (articuloActual != null) {
            guardarArticulo(articuloActual, buffer.toString(), ++total);
        }

        System.out.println("🎯 Carga completa. Total de artículos procesados: " + total);
    }

    /**
     * ✅ Modo avanzado: recibe una lista de artículos (útil si ya lo extraíste en memoria)
     */
    public void subirCodigoCivil(List<String> articulos) throws Exception {
        int total = articulos.size();
        System.out.println("📘 Total de artículos a subir: " + total);

        for (int i = 0; i < total; i++) {
            String articulo = articulos.get(i);
            int actual = i + 1;

            try {
                subirArticuloConProgreso(articulo, actual, total);
            } catch (Exception e) {
                System.err.printf("❌ [%d/%d] Error al subir artículo: %s%n", actual, total, e.getMessage());
            }

            // 🕐 Pequeño delay para evitar saturar OpenAI
            Thread.sleep(400);
        }

        System.out.println("🎯 Finalizado: se procesaron todos los artículos (" + total + ")");
    }

    /**
     * 🔹 Subida individual con logs y progreso
     */
    private void subirArticuloConProgreso(String articulo, int actual, int total) throws Exception {
        System.out.printf("➡️ [%d/%d] Procesando '%s'%n", actual, total, extraerTitulo(articulo));

        List<Float> embedding = embeddingService.generarEmbedding(articulo);

        Map<String, Object> body = Map.of(
                "articulo", extraerTitulo(articulo),
                "contenido", articulo,
                "embedding", embedding
        );

        String json = mapper.writeValueAsString(List.of(body));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/codigo_civil_embeddings")
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String bodyError = response.body() != null ? response.body().string() : "(sin cuerpo)";
                throw new IOException("Supabase Error: " + response.code() + " - " + bodyError);
            }
        }

        System.out.printf("✅ [%d/%d] Subido correctamente.%n", actual, total);
    }

    /**
     * 🔹 Inserta un solo artículo (usado por el método subirCodigo)
     */
    private void guardarArticulo(String articulo, String contenido, int numero) throws Exception {
        try {
            List<Float> embedding = embeddingService.generarEmbedding(contenido);

            Map<String, Object> body = Map.of(
                    "articulo", articulo,
                    "contenido", contenido,
                    "embedding", embedding
            );

            String json = mapper.writeValueAsString(List.of(body));

            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/codigo_civil_embeddings")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String error = response.body() != null ? response.body().string() : "";
                    throw new IOException("Supabase Error: " + response.code() + " - " + error);
                }
            }

            System.out.printf("✅ [%d] Subido %s%n", numero, articulo);

        } catch (Exception e) {
            System.err.printf("❌ [%d] Error al subir %s: %s%n", numero, articulo, e.getMessage());
        }
    }

    /**
     * 🔹 Devuelve un título acortado (usado para logs o la columna 'articulo')
     */
    private String extraerTitulo(String texto) {
        String[] lineas = texto.split("\n");
        if (lineas.length > 0) {
            String primera = lineas[0].trim();
            return primera.length() > 100 ? primera.substring(0, 100) + "..." : primera;
        }
        return texto.length() > 100 ? texto.substring(0, 100) + "..." : texto;
    }
}