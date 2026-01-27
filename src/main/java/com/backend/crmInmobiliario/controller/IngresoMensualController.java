package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.salida.IngresoMensualSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.IngresoMensualResumenDto;
import com.backend.crmInmobiliario.service.impl.IngresoMensualService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RequestMapping("/api/ingresos")
@CrossOrigin(origins = "https://tuinmo.net")
@RestController
@AllArgsConstructor
public class IngresoMensualController {

    private final IngresoMensualService ingresoMensualService;
    private final AuthUtil authUtil;

    // 🔹 1️⃣ Genera los ingresos mensuales del mes actual (snapshot)
    // ✅ Generar ingresos del mes actual usando el userId del token
    @PostMapping("/generar")
    public ResponseEntity<String> generarIngresosDelMesActual() {
        Long userId = authUtil.extractUserId();
        ingresoMensualService.generarIngresosDelMesActual(userId);
        return ResponseEntity.ok("✅ Ingresos del mes actual generados correctamente.");
    }



    // 🔹 2️⃣ Obtiene todos los ingresos del usuario en un mes/año
    @GetMapping("/mensuales")
    public List<IngresoMensualSalidaDto> obtenerPorMesYAnio(
            @RequestParam int mes,
            @RequestParam int anio) {
        return ingresoMensualService.obtenerPorMesYAnio(mes, anio);
    }

    // 🔹 3️⃣ Obtiene resumen anual (para el gráfico)
    @GetMapping("/anual")
    public List<IngresoMensualResumenDto> obtenerResumenAnual(
            @RequestParam int anio,
            @RequestParam Long userId) {

        return ingresoMensualService.obtenerResumenAnual(userId, anio);
    }

    @PostMapping("/regenerar")
    public ResponseEntity<String> regenerarIngresos() {
        ingresoMensualService.regenerarIngresosFaltantes();
        return ResponseEntity.ok("✅ Ingresos faltantes regenerados exitosamente");
    }


    @PostMapping("/sync-supabase")
    public ResponseEntity<?> syncSupabase() {
        try {
            ingresoMensualService.sincronizarIngresosExistentesConSupabase();
            return ResponseEntity.ok(Map.of(
                    "message", "Sincronización de ingresos con Supabase completada"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

}
