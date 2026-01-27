package com.backend.crmInmobiliario.service.impl.providers;

import com.backend.crmInmobiliario.DTO.modificacion.ReciboEstadoActualizadoEvent;
import com.backend.crmInmobiliario.entity.Recibo;
import com.backend.crmInmobiliario.repository.ReciboRepository;
import com.backend.crmInmobiliario.repository.projections.ReciboSyncProjection;
import com.backend.crmInmobiliario.service.impl.ContratoService;
import com.backend.crmInmobiliario.service.impl.ReciboService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ReciboSyncListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReciboSyncListener.class);

    private final ReciboRepository reciboRepository;
    private final ReciboService reciboService;
    private final ContratoService contratoService;

    public ReciboSyncListener(ReciboRepository reciboRepository,
                              ReciboService reciboService,
                              ContratoService contratoService) {
        this.reciboRepository = reciboRepository;
        this.reciboService = reciboService;
        this.contratoService = contratoService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ReciboEstadoActualizadoEvent e) {
        try {
            ReciboSyncProjection data = reciboRepository.findSyncData(e.reciboId());

            if (data == null) { // ✅ ojo: == null, no = null
                LOGGER.warn("⚠️ No se encontró info para recibo {}", e.reciboId());
                return;
            }

            reciboService.guardarReciboEnSupabasePorProjection(data);
            reciboService.upsertReciboEmbeddingPorProjection(data);
            contratoService.actualizarContratoEnSupabasePorId(data.getContratoId());

            LOGGER.info("✅ Sync OK recibo {} contrato {}", data.getIdRecibo(), data.getContratoId());
        } catch (Exception ex) {
            LOGGER.error("❌ Error async al sincronizar recibo {}: {}", e.reciboId(), ex.getMessage(), ex);
        }
    }
}
