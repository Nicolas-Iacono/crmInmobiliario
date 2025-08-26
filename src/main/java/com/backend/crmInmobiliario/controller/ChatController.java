package com.backend.crmInmobiliario.controller;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;



@CrossOrigin(
        origins = "http://localhost:3000",
        allowedHeaders = { "Content-Type", "x-user-id", "x-username", "Authorization", "Accept", "Origin" },
        methods = { RequestMethod.POST, RequestMethod.OPTIONS },
        allowCredentials = "true" // en Spring suele ser String
)
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final OkHttpClient http = new OkHttpClient();
    private final UsuarioRepository usuarioRepository;

    private static final String N8N_WEBHOOK_URL =
            "https://primary-production-9170b.up.railway.app/webhook/31431941-8c7f-4e58-a45f-6de60de45825/chat";

    public ChatController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository; // ✅ inyección por constructor
    }

    @PostMapping
    public ResponseEntity<String> chat(
            @RequestBody String payload,
            @RequestHeader(value = "x-user-id", required = false) Long uidHeader,
            @RequestHeader(value = "x-username", required = false) String unameHeader
    ) {
        try {
            // 1) Resolver identidad
            Long resolvedUserId = uidHeader;
            String resolvedUsername = unameHeader;

            // a) Si falta username pero usás Spring Security, intentar tomarlo del contexto
            if (resolvedUsername == null) {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
                    resolvedUsername = auth.getName();
                }
            }

            // b) Si falta userId pero tengo username → buscar en DB
            if (resolvedUserId == null && resolvedUsername != null && !resolvedUsername.isBlank()) {
                var userOpt = usuarioRepository.findUserByUsername(resolvedUsername);
                if (userOpt.isPresent()) {
                    resolvedUserId = userOpt.get().getId();
                }
            }

            // c) Si aún falta userId → último recurso: 0 (o cortar con 400)
            if (resolvedUserId == null) {
                // Podés devolver 400 si querés forzar identidad:
                // return ResponseEntity.badRequest().body("{\"error\":\"Falta x-user-id y no se pudo resolver\"}");
                resolvedUserId = 0L; // evita NPE y te permite ver el flujo completo
            }

            if (resolvedUsername == null || resolvedUsername.isBlank()) {
                resolvedUsername = "desconocido";
            }

            System.out.println("USER ID RESUELTO: " + resolvedUserId);
            System.out.println("USERNAME RESUELTO: " + resolvedUsername);

            // 2) Asegurar que n8n reciba el user también en el BODY (por si lo leés desde $json.user_id)
            //    Mezclamos el payload original y añadimos user_id/username si no existen
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode bodyJson;

            try {
                bodyJson = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(payload);
            } catch (Exception ex) {
                // Si no es JSON válido, creamos uno nuevo con el texto original como "message"
                bodyJson = mapper.createObjectNode();
                bodyJson.put("message", payload);
            }

            if (!bodyJson.has("user_id")) bodyJson.put("user_id", resolvedUserId);
            if (!bodyJson.has("username")) bodyJson.put("username", resolvedUsername);

            String finalPayload = mapper.writeValueAsString(bodyJson);

            // 3) Construir request hacia n8n
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    finalPayload,
                    okhttp3.MediaType.get("application/json")
            );

            okhttp3.Request req = new okhttp3.Request.Builder()
                    .url(N8N_WEBHOOK_URL)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-user-id", String.valueOf(resolvedUserId))
                    .addHeader("x-username", resolvedUsername)
                    .post(body)
                    .build();

            // 4) Ejecutar y devolver tal cual
            try (okhttp3.Response resp = http.newCall(req).execute()) {
                String responseBody = resp.body() != null ? resp.body().string() : "";
                return ResponseEntity.status(resp.code())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(responseBody.isBlank() ? "{\"reply\":\"Sin respuesta\"}" : responseBody);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Error al conectar con el webhook\"}");
        }
    }
}