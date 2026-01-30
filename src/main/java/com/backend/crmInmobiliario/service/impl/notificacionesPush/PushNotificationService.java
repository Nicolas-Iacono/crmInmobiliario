package com.backend.crmInmobiliario.service.impl.notificacionesPush;

import com.backend.crmInmobiliario.entity.PushSubscription;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PushNotificationService {

    private final PushService pushService;

    public PushNotificationService(
            @Value("${vapid.public}") String vapidPublic,
            @Value("${vapid.private}") String vapidPrivate,
            @Value("${vapid.subject}") String subject
    ) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {

        // Registrar BouncyCastle
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        // ESTA ES LA FORMA CORRECTA PARA TU VERSIÓN
        this.pushService = new PushService();
        this.pushService.setPublicKey(vapidPublic);
        this.pushService.setPrivateKey(vapidPrivate);
        this.pushService.setSubject(subject);

        log.info("🔐 PushService inicializado con claves RAW (String)");
    }

    public void enviarNotificacion(PushSubscription sub, String titulo, String cuerpo) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "GENERICA");
        data.put("contratoId", null);
        data.put("notaId", null);
        enviarNotificacion(sub, titulo, cuerpo, data);
    }

    public void enviarNotificacion(PushSubscription sub, String titulo, String cuerpo, Map<String, Object> data) {
        try {
            // Armamos JSON manual simple (sin ObjectMapper) para evitar dependencias acá
            Long contratoId = data.get("contratoId") != null ? Long.valueOf(data.get("contratoId").toString()) : null;
            Long notaId = data.get("notaId") != null ? Long.valueOf(data.get("notaId").toString()) : null;
            String type = data.get("type") != null ? data.get("type").toString() : null;

            String payload = """
        {
          "title": "%s",
          "body": "%s",
          "data": {
            "type": "%s",
            "contratoId": %s,
            "notaId": %s
          }
        }
        """.formatted(
                    escapeJson(titulo),
                    escapeJson(cuerpo),
                    escapeJson(type != null ? type : "GENERICA"),
                    contratoId != null ? contratoId.toString() : "null",
                    notaId != null ? notaId.toString() : "null"
            );

            Notification notification = new Notification(
                    sub.getEndpoint(),
                    sub.getP256dh(),
                    sub.getAuth(),
                    payload.getBytes()
            );

            pushService.send(notification);
            log.info("📨 Notificación enviada correctamente");

        } catch (Exception e) {
            log.error("❌ Error enviando notificación:", e);
        }
    }

    // helper mínimo para no romper JSON con comillas
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public void enviarNotificacionConIcono(PushSubscription sub, String titulo, String cuerpo, String iconUrl) {
        try {
            String iconPart = iconUrl != null && !iconUrl.isBlank()
                    ? String.format(", \"icon\": \"%s\"", iconUrl)
                    : "";
            String payload = String.format("{\"title\": \"%s\", \"body\": \"%s\"%s}", titulo, cuerpo, iconPart);
            Notification notification = new Notification(
                    sub.getEndpoint(),
                    sub.getP256dh(),
                    sub.getAuth(),
                    payload.getBytes()
            );
            pushService.send(notification);
            System.out.println("✅ Notificación enviada correctamente");
        } catch (IOException | GeneralSecurityException |
                 org.jose4j.lang.JoseException |
                 java.util.concurrent.ExecutionException |
                 InterruptedException e) {
            System.err.println("❌ Error al enviar notificación push:");
            e.printStackTrace();
        }
    }
}