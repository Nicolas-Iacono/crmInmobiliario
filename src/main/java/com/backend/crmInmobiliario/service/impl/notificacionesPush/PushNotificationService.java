package com.backend.crmInmobiliario.service.impl.notificacionesPush;

import com.backend.crmInmobiliario.entity.PushSubscription;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;

@Service
public class PushNotificationService {

    private final PushService pushService;

    public PushNotificationService(
            @Value("${vapid.public}") String publicKey,
            @Value("${vapid.private}") String privateKey,
            @Value("${vapid.subject}") String subject
    ) throws GeneralSecurityException {
        // ✅ Registrar el proveedor BouncyCastle si no está ya cargado
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
            System.out.println("🟢 BouncyCastle provider registrado");
        }

        this.pushService = new PushService();
        this.pushService.setPublicKey(publicKey);
        this.pushService.setPrivateKey(privateKey);
        this.pushService.setSubject(subject);
    }

    public void enviarNotificacion(PushSubscription sub, String titulo, String cuerpo) {
        try {
            String payload = String.format("{\"title\": \"%s\", \"body\": \"%s\"}", titulo, cuerpo);
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
