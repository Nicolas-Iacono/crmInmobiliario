//package com.backend.crmInmobiliario.controller;
//
//import com.backend.crmInmobiliario.DTO.entrada.planesYSuscripcion.CheckoutResponse;
//import com.backend.crmInmobiliario.DTO.entrada.planesYSuscripcion.SubscriptionMeDto;
//import com.backend.crmInmobiliario.entity.Usuario;
//import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
//import com.backend.crmInmobiliario.repository.ContratoRepository;
//import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
//import com.backend.crmInmobiliario.repository.pagosYSuscripciones.SubscriptionRepository;
//import com.backend.crmInmobiliario.service.impl.SubscriptionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/subscriptions")
//@RequiredArgsConstructor
//public class SubscriptionController {
//    private final SubscriptionService service;
//    private final PlanRepository plans;
//    private final ContratoRepository contratos;
//    private final SubscriptionRepository subsRepo;
//
//    @GetMapping("/plans")
//    public List<Plan> listPlans() { return plans.findAllByActiveTrueOrderByPriceUsdAsc(); }
//
//    @GetMapping("/me")
//    public SubscriptionMeDto me(@AuthenticationPrincipal Usuario authUser) {
//        var dto = service.getLimits(authUser.getId());
//        long activos = contratos.countActivosByUsuario(authUser.getId());
//        return new SubscriptionMeDto(
//                dto.planCode(), dto.planName(), dto.status().name(),
//                dto.contractLimit(), activos, dto.trialEndsAt(), dto.currentPeriodEnd(), dto.cancelAtPeriodEnd()
//        );
//    }
//
//    @PostMapping("/checkout")
//    public CheckoutResponse checkout(@AuthenticationPrincipal Usuario u, @RequestBody CheckoutRequest req) {
//        return service.initCheckout(u.getId(), req.planCode());
//    }
//
//    @PostMapping("/change-plan")
//    public void changePlan(@AuthenticationPrincipal Usuario u, @RequestBody ChangePlanRequest req) {
//        service.changePlan(u.getId(), req.planCode());
//    }
//
//    @PostMapping("/cancel")
//    public void cancelNow(@AuthenticationPrincipal Usuario u) { service.cancelNow(u.getId()); }
//
//    @PostMapping("/cancel-at-period-end")
//    public void cancelAtPeriodEnd(@AuthenticationPrincipal Usuario u, @RequestBody CancelAtPeriodEndRequest req) {
//        service.setCancelAtPeriodEnd(u.getId(), req.cancelAtPeriodEnd());
//    }
//}
//
