package com.backend.crmInmobiliario.controller.providerControllers;

import com.backend.crmInmobiliario.DTO.salida.inquilino.MpInitPointResponse;
import com.backend.crmInmobiliario.service.impl.mercadoPago.MercadoPagoReciboService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos/mercadopago")
public class MercadoPagoReciboController {
    private final AuthUtil authUtil;
    private final MercadoPagoReciboService mpService;

    public MercadoPagoReciboController(MercadoPagoReciboService mpService,AuthUtil authUtil) {
        this.mpService = mpService;
        this.authUtil = authUtil;
    }

    @PostMapping("/recibos/{reciboId}/preferencia")
    public MpInitPointResponse crearPreferencia(@PathVariable Long reciboId) throws Exception {
        Long userIdInquilino = authUtil.extractUserId();

        return mpService.crearLinkPagoRecibo(reciboId, userIdInquilino);
    }
}

