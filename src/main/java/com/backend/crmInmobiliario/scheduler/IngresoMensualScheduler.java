package com.backend.crmInmobiliario.scheduler;

import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.impl.IngresoMensualService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class IngresoMensualScheduler {

    private final IngresoMensualService ingresoMensualService;
    private final UsuarioRepository usuarioRepository;

    public IngresoMensualScheduler(IngresoMensualService ingresoMensualService,
                                   UsuarioRepository usuarioRepository) {
        this.ingresoMensualService = ingresoMensualService;
        this.usuarioRepository = usuarioRepository;
    }

    // 🔹 Ejecuta el día 1 de cada mes a las 3:00 AM
    @Scheduled(cron = "0 0 3 1 * ?")
    public void generarIngresosMensualesAutomaticamente() {
        LocalDate hoy = LocalDate.now();
        int mes = hoy.getMonthValue();
        int anio = hoy.getYear();

        log.info("🕒 Generando ingresos mensuales automáticos para {} / {}", mes, anio);

        List<Usuario> usuarios = usuarioRepository.findAll();

        for (Usuario usuario : usuarios) {
            try {
                ingresoMensualService.generarIngresosDelMesActual(usuario.getUsername());
                log.info("✅ Ingresos generados para usuario {}", usuario.getUsername());
            } catch (Exception e) {
                log.error("❌ Error al generar ingresos para usuario {}: {}", usuario.getUsername(), e.getMessage());
            }
        }

        log.info("🏁 Generación automática de ingresos finalizada.");
    }
}
