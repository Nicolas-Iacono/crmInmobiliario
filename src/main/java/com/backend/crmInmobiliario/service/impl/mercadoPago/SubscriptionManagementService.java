package com.backend.crmInmobiliario.service.impl.mercadoPago;

import com.backend.crmInmobiliario.DTO.mpDtos.CheckoutResponse;
import com.backend.crmInmobiliario.DTO.mpDtos.PlanLimitsDto;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Subscription;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
@Service
@RequiredArgsConstructor
public class SubscriptionManagementService {


    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContratoRepository contratoRepository;
    private final PaymentService paymentService;


    @Transactional
    public CheckoutResponse initCheckout(Long usuarioId, String planCode) {
        Plan plan = planRepository.findByCodeAndActiveTrue(planCode)
                .orElseThrow(() -> new ResourceNotFoundException("Plan inválido: " + planCode));


        Usuario user = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));


        subscriptionRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> bootstrapFree(user));


        String redirectUrl = paymentService.initSubscriptionCheckout(usuarioId, planCode).getInitPoint();
        return new CheckoutResponse(redirectUrl);
    }


    @Transactional
    public void cancelNow(Long usuarioId) {
        Subscription sub = subscriptionRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada"));


        sub.setStatus(Subscription.Status.CANCELED);
        sub.setCancelAtPeriodEnd(false);
        subscriptionRepository.save(sub);


        moveToFreeIfNeeded(usuarioId);
    }


    @Transactional
    public void changePlan(Long usuarioId, String newPlanCode) {
        Subscription sub = subscriptionRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada"));


        Plan target = planRepository.findByCodeAndActiveTrue(newPlanCode)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no disponible: " + newPlanCode));


        long activos = contratoRepository.countActivosByUsuario(usuarioId);
        if (target.getContractLimit() != null && target.getContractLimit() >= 0 && activos > target.getContractLimit()) {
            throw new IllegalStateException("Tienes " + activos + " contratos activos y el límite del plan es " + target.getContractLimit());
        }


        sub.setPlan(target);
        subscriptionRepository.save(sub);
    }

    @Transactional
    public PlanLimitsDto getLimits(Long usuarioId) {
        return subscriptionRepository.findByUsuarioId(usuarioId)
                .map(this::mapSubscriptionToDto)
                .orElseGet(() -> {
                    Plan free = planRepository.findByCode("FREE")
                            .orElseThrow(() -> new ResourceNotFoundException("Plan FREE no encontrado"));
                    return new PlanLimitsDto(
                            free.getCode(),
                            free.getName(),
                            free.getContractLimit(),
                            Subscription.Status.ACTIVE,
                            null,
                            null,
                            false
                    );
                });
    }
    private PlanLimitsDto mapSubscriptionToDto(Subscription sub) {
        return new PlanLimitsDto(
                sub.getPlan().getCode(),
                sub.getPlan().getName(),
                sub.getPlan().getContractLimit(),
                sub.getStatus(),
                sub.getTrialEndsAt() != null ? sub.getTrialEndsAt().atZone(ZoneOffset.UTC).toInstant() : null,
                sub.getCurrentPeriodEnd() != null ? sub.getCurrentPeriodEnd().atZone(ZoneOffset.UTC).toInstant() : null,
                Boolean.TRUE.equals(sub.getCancelAtPeriodEnd())
        );
    }




    @Transactional
    public void setCancelAtPeriodEnd(Long usuarioId, boolean cancelAtPeriodEnd) {
        Subscription sub = subscriptionRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada"));


        sub.setCancelAtPeriodEnd(cancelAtPeriodEnd);
        subscriptionRepository.save(sub);
    }


    @Transactional
    protected Subscription bootstrapFree(Usuario user) {
        Plan free = planRepository.findByCode("FREE")
                .orElseThrow(() -> new ResourceNotFoundException("Plan FREE no encontrado"));


        Subscription s = Subscription.builder()
                .usuario(user)
                .plan(free)
                .status(Subscription.Status.ACTIVE)
                .cancelAtPeriodEnd(false)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();


        return subscriptionRepository.save(s);
    }


    @Transactional
    protected void moveToFreeIfNeeded(Long usuarioId) {
        Subscription sub = subscriptionRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada"));


        Plan free = planRepository.findByCode("FREE")
                .orElseThrow(() -> new ResourceNotFoundException("Plan FREE no encontrado"));


        sub.setPlan(free);
        sub.setStatus(Subscription.Status.ACTIVE);
        sub.setCancelAtPeriodEnd(false);
        sub.setCurrentPeriodEnd(null);
        sub.setTrialEndsAt(null);
        sub.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        subscriptionRepository.save(sub);
    }
}