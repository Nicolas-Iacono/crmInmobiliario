package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.service.impl.IA.ChatBotService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "https://tuinmo.net")
@RestController
@AllArgsConstructor
@RequestMapping("api/chat")
public class IASupabaseChatController {
    private ChatBotService chatBotService;

    @PostMapping("/preguntar")
    public ResponseEntity<?> consultar(@RequestBody PreguntaRequest req) {
        try {
            String modo = (req.getModo() == null || req.getModo().isBlank()) ? "mixto" : req.getModo();
            String respuesta = chatBotService.responderPregunta(req.getPregunta(), modo);

            return ResponseEntity.ok(Map.of(
                    "modo", modo,
                    "respuesta", respuesta
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error al procesar la consulta: " + e.getMessage()
            ));
        }
    }

    @Data
    public static class PreguntaRequest {
        private String pregunta;
        private String modo; // "mixto", "legales", "datos"
    }



}

