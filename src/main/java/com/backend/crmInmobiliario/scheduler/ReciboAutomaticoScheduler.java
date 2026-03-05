package com.backend.crmInmobiliario.scheduler;

import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.ModoRecibos;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.service.impl.ReciboService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReciboAutomaticoScheduler {

    private final ContratoRepository contratoRepository;
    private final ReciboService reciboService;

    @Scheduled(cron = "0 0 3 * * ?", zone = "America/Argentina/Buenos_Aires")
    public void generarRecibosAutomaticos() {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        String periodo = hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "AR")) + " " + hoy.getYear();

        contratoRepository.findByModoRecibosAndAutoRecibosActivoTrueAndActivoTrue(ModoRecibos.AUTOMATICO)
                .stream()
                .filter(c -> c.getDiaGeneracion() == hoy.getDayOfMonth())
                .forEach(c -> generarParaContrato(c, periodo));
    }

    private void generarParaContrato(Contrato contrato, String periodo) {
        try {
            reciboService.crearReciboAutomaticoSistema(contrato.getId(), periodo);
            log.info("Recibo automático generado. contratoId={}, periodo={}", contrato.getId(), periodo);
        } catch (IllegalStateException duplicated) {
            log.debug("Recibo automático ya existía. contratoId={}, periodo={}", contrato.getId(), periodo);
        } catch (Exception e) {
            log.error("Error generando recibo automático. contratoId={}, periodo={}", contrato.getId(), periodo, e);
        }
    }
}
