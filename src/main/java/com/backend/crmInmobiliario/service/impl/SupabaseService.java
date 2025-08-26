package com.backend.crmInmobiliario.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupabaseService {
    private final String apiKey = System.getenv("OPENAI_API_KEY");
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final String supabaseUrl = "https://<tu-proyecto>.supabase.co";
    private final String supabaseKey = System.getenv("SUPABASE_KEY");

    public void insertarDocumentoEmbedding(String tipo,
                                           Long referenciaId,
                                           Long userId,
                                           String username,
                                           String contenido,
                                           List<Float> embedding) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode json = mapper.createObjectNode();
            json.put("tipo", tipo);
            json.put("referencia_id", referenciaId);
            json.put("user_id", userId);
            json.put("username", username);
            json.put("contenido", contenido);
            json.set("embedding", mapper.valueToTree(embedding));

            String body = mapper.writeValueAsString(json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/rest/v1/documentos_embeddings"))
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new RuntimeException("Error al insertar en Supabase: " + response.body());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error insertando documento en Supabase", e);
        }
    }

    public List<Float> generarEmbedding(String texto) {
        try {
            String body = """
            {
              "model": "text-embedding-3-small",
              "input": "%s"
            }
            """.formatted(texto.replace("\"","\\\""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/embeddings"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Parsear JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode arr = root.get("data").get(0).get("embedding");

            List<Float> vector = new ArrayList<>();
            arr.forEach(node -> vector.add(node.floatValue()));

            return vector;

        } catch (Exception e) {
            throw new RuntimeException("Error generando embedding en OpenAI", e);
        }
    }
}