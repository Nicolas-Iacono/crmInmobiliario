// ✅ SubscriptionController.java
package com.backend.crmInmobiliario.controller.providerControllers;


import com.backend.crmInmobiliario.DTO.mpDtos.CheckoutRequest;
import com.backend.crmInmobiliario.DTO.mpDtos.CheckoutResponse;
import com.backend.crmInmobiliario.DTO.mpDtos.SubscriptionMeDto;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.SubscriptionRepository;
import com.backend.crmInmobiliario.service.impl.mercadoPago.SubscriptionManagementService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionManagementService service;
    private final PlanRepository plans;
    private final ContratoRepository contratos;
    private final SubscriptionRepository subsRepo;
    private final UsuarioRepository usuarioRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/plans")
    public List<Plan> listPlans() {
        return plans.findAllByActiveTrueOrderByPriceArsAsc();
    }

    @GetMapping("/me")
    public SubscriptionMeDto me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        String username = authentication.getName();
        var user = usuarioRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        var dto = service.getLimits(user.getId());
        long activos = contratos.countActivosByUsuario(user.getId());

        return new SubscriptionMeDto(
                username, dto.planCode(), dto.planName(), dto.status().name(),
                dto.contractLimit(), activos, dto.trialEndsAt(), dto.currentPeriodEnd(), dto.cancelAtPeriodEnd()
        );
    }

    @PostMapping("/checkout")
    public CheckoutResponse checkout(Authentication authentication, @RequestBody CheckoutRequest req) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        String username = authentication.getName();
        var user = usuarioRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (req == null || req.getPlanCode() == null || req.getPlanCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "planCode requerido");
        }

        logger.info("Checkout para usuario {}", user.getId());
        logger.info("Plan: {}", req.getPlanCode().trim());
        return service.initCheckout(user.getId(), req.getPlanCode().trim());
    }

//    @PostMapping("/change-plan")
//    public void changePlan(Authentication authentication, @RequestBody ChangePlanRequest req) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
//        }
//
//        var user = usuarioRepository.findUserByUsername(authentication.getName())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
//
//        if (req == null || req.getPlanCode() == null || req.getPlanCode().isBlank()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "planCode requerido");
//        }
//
//        service.changePlan(user.getId(), req.getPlanCode().trim());
//    }
//
//    @PostMapping("/cancel")
//    public void cancelNow(Authentication authentication) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
//        }
//
//        var user = usuarioRepository.findUserByUsername(authentication.getName())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
//
//        service.cancelNow(user.getId());
//    }
//
//    @PostMapping("/cancel-at-period-end")
//    public void cancelAtPeriodEnd(Authentication authentication, @RequestBody CancelAtPeriodEndRequest req) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
//        }
//
//        var user = usuarioRepository.findUserByUsername(authentication.getName())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
//
//        service.setCancelAtPeriodEnd(user.getId(), req.cancelAtPeriodEnd());
//    }
}
