package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.entity.PushSubscription;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.notificacionesPush.PushSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class PushNotificationController {

    @Autowired
    private PushSubscriptionRepository subscriptionRepo;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody PushSubscription request, @RequestParam Long userId) {
        try {
            Usuario usuario = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

            PushSubscription sub = new PushSubscription();
            sub.setEndpoint(request.getEndpoint());
            sub.setP256dh(request.getP256dh());
            sub.setAuth(request.getAuth());
            sub.setUserId(usuario.getId());

            subscriptionRepo.save(sub);

            return ResponseEntity.ok("✅ Suscripción registrada correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Error al registrar la suscripción");
        }
    }
}
