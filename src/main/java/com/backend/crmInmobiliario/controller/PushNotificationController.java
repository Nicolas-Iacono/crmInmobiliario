package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.PushSubscriptionRequest;
import com.backend.crmInmobiliario.entity.PushSubscription;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.notificacionesPush.PushSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class PushNotificationController {

    @Autowired
    private PushSubscriptionRepository subscriptionRepo;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody PushSubscriptionRequest request,
                                       @RequestParam Long userId) {
        try {
            Usuario usuario = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // 1) Si el endpoint ya existe, lo actualizo (re-asocio al usuario por las dudas)
            PushSubscription sub = subscriptionRepo.findByEndpoint(request.getEndpoint())
                    .orElse(null);

            if (sub != null) {
                sub.setUserId(usuario.getId());
                sub.setP256dh(request.getKeys().getP256dh());
                sub.setAuth(request.getKeys().getAuth());
                subscriptionRepo.save(sub);
                return ResponseEntity.ok("✅ Suscripción actualizada");
            }

            // 2) Si es endpoint nuevo: aplicar límite de 4
            long count = subscriptionRepo.countByUserId(usuario.getId());
            if (count >= 4) {
                subscriptionRepo.findFirstByUserIdOrderByCreatedAtAsc(usuario.getId())
                        .ifPresent(subscriptionRepo::delete); // borro el más viejo
            }

            // 3) Crear nueva
            PushSubscription nueva = new PushSubscription();
            nueva.setUserId(usuario.getId());
            nueva.setEndpoint(request.getEndpoint());
            nueva.setP256dh(request.getKeys().getP256dh());
            nueva.setAuth(request.getKeys().getAuth());
            // createdAt se setea solo por @PrePersist

            subscriptionRepo.save(nueva);

            return ResponseEntity.ok("✅ Suscripción registrada (máx 4 dispositivos)");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Error al registrar suscripción");
        }
    }
}
