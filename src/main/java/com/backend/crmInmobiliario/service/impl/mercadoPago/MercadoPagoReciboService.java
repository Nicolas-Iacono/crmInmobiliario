package com.backend.crmInmobiliario.service.impl.mercadoPago;

import com.backend.crmInmobiliario.DTO.salida.inquilino.MpInitPointResponse;
import com.backend.crmInmobiliario.entity.Recibo;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.ReciboRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MercadoPagoReciboService {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.webhook-url}")
    private String webhookUrl;

    @Value("${mercadopago.success-url}")
    private String successUrl;

    @Value("${mercadopago.failure-url}")
    private String failureUrl;

    @Value("${mercadopago.pending-url}")
    private String pendingUrl;

    private final ReciboRepository reciboRepository;
    private final UsuarioRepository usuarioRepository;

    public MercadoPagoReciboService(ReciboRepository reciboRepository, UsuarioRepository usuarioRepository) {
        this.reciboRepository = reciboRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public MpInitPointResponse crearLinkPagoRecibo(Long reciboId, Long userInquilinoId) throws Exception {
        Usuario usuario = usuarioRepository.findUserById(userInquilinoId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getInquilino() == null) {
            throw new RuntimeException("Solo un usuario inquilino puede pagar recibos");
        }

        Recibo recibo = reciboRepository.findById(reciboId)
                .orElseThrow(() -> new RuntimeException("Recibo no encontrado"));

        // Validar pertenencia
        Long inqIdRecibo = recibo.getContrato().getInquilino().getId();
        Long inqIdUsuario = usuario.getInquilino().getId();
        if (!inqIdRecibo.equals(inqIdUsuario)) {
            throw new RuntimeException("Este recibo no pertenece a tu cuenta");
        }

        if (Boolean.TRUE.equals(recibo.getEstado())) {
            throw new RuntimeException("El recibo ya está pago");
        }

        MercadoPagoConfig.setAccessToken(accessToken);

        BigDecimal monto = recibo.getMontoTotal() != null ? recibo.getMontoTotal() : BigDecimal.ZERO;

        String externalRef = "RECIBO-" + recibo.getId() + "-USR-" + usuario.getId();

        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title("Pago de recibo " + recibo.getNumeroRecibo() + " (" + recibo.getPeriodo() + ")")
                .quantity(1)
                .unitPrice(monto)
                .currencyId("ARS")
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(successUrl)
                .failure(failureUrl)
                .pending(pendingUrl)
                .build();

        PreferenceRequest request = PreferenceRequest.builder()
                .items(List.of(item))
                .externalReference(externalRef)
                .notificationUrl(webhookUrl)
                .backUrls(backUrls)
                // auto_return: cuando se acredita, vuelve solo (opcional)
                .autoReturn("approved")
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference pref = client.create(request);

        // Guardar trazabilidad en el recibo
        recibo.setMpPreferenceId(pref.getId());
        recibo.setMpExternalReference(externalRef);
        reciboRepository.save(recibo);

        return new MpInitPointResponse(pref.getInitPoint(), pref.getId());
    }
}