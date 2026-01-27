package com.backend.crmInmobiliario.service.impl.IA;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.key}")
    private String SUPABASE_ANON_KEY;

    @Value("${supabase.service.role.key}")
    private String SUPABASE_SERVICE_ROLE_KEY;

    @Value("${openai.api.key}")
    private String OPENAI_APIKEY;

    private static EmbeddingService instance;

    @PostConstruct
    public void init() {
        configure(this);
    }


    public static void configure(EmbeddingService service) {
        instance = service;
    }

    public static List<Float> generarEmbeddingStatic(String texto) throws Exception {
        if (instance == null) {
            throw new IllegalStateException("❌ EmbeddingService no está configurado. Llama a configure() después de crear el bean.");
        }
        return instance.generarEmbedding(texto);
    }

    public List<Float> generarEmbedding(String texto) throws IOException, InterruptedException {

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(Map.of(
                "model", "text-embedding-3-small",
                "input", texto
        ));

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/embeddings")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + OPENAI_APIKEY)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("❌ OpenAI Error: " +
                        response.code() + " - " + response.message());
            }

            JsonNode vector = mapper.readTree(response.body().string())
                    .path("data").get(0).path("embedding");

            List<Float> result = new ArrayList<>();
            for (JsonNode n : vector) result.add(n.floatValue());

            return result;
        }
    }
}
