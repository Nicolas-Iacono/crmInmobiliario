package com.backend.crmInmobiliario.utils;

import com.backend.crmInmobiliario.DTO.ContratoCreadoEvent;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.service.impl.ContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class ContratoCreadoListener {

    private final ContratoService contratoService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onContratoCreado(ContratoCreadoEvent ev) {
        try {
            contratoService.actualizarContratoEnSupabasePorId(ev.contratoId());
        } catch (Exception e) {
            // IMPORTANTE: no romper el flujo principal (el contrato ya está creado en SQL)
            // Solo loguear / notificar.
            // LOGGER.error("⚠️ Error sincronizando contrato {} en Supabase", ev.contratoId(), e);
        }
    }
}

