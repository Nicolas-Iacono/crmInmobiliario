package com.backend.crmInmobiliario.service.impl.providers;

import com.backend.crmInmobiliario.DTO.entrada.contrato.ContratoActualizadoEvent;
import com.backend.crmInmobiliario.service.impl.ContratoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContratoSyncListener {
    private final ContratoService contratoService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onContratoActualizado(ContratoActualizadoEvent event) {
        try {
            contratoService.actualizarContratoEnSupabasePorId(event.contratoId());
        } catch (Exception e) {
            log.error("❌ Error al sincronizar contrato {} con Supabase: {}", event.contratoId(), e.getMessage());
        }
    }
}