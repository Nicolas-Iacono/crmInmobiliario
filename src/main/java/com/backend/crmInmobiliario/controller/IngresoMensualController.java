package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.salida.IngresoMensualSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.IngresoMensualResumenDto;
import com.backend.crmInmobiliario.service.impl.IngresoMensualService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RequestMapping("/api/ingresos")
@CrossOrigin(origins = "https://tuinmo.net")
@RestController
@AllArgsConstructor
public class IngresoMensualController {

    private final IngresoMensualService ingresoMensualService;


    // 🔹 1️⃣ Genera los ingresos mensuales del mes actual (snapshot)
    @PostMapping("/generar")
    public ResponseEntity<String> generarIngresosDelMesActual(Authentication authentication) {
        // 🔹 Obtener el nombre de usuario del contexto de seguridad
        String username = authentication.getName();

        ingresoMensualService.generarIngresosDelMesActual(username);
        return ResponseEntity.ok("Ingresos del mes actual generados correctamente para el usuario " + username);
    }



    // 🔹 2️⃣ Obtiene todos los ingresos del usuario en un mes/año
    @GetMapping("/mensuales")
    public List<IngresoMensualSalidaDto> obtenerPorMesYAnio(
            @RequestParam int mes,
            @RequestParam int anio,
            @RequestParam String username) {

        return ingresoMensualService.obtenerPorMesYAnio(username, mes, anio);
    }

    // 🔹 3️⃣ Obtiene resumen anual (para el gráfico)
    @GetMapping("/anual")
    public List<IngresoMensualResumenDto> obtenerResumenAnual(
            @RequestParam int anio,
            @RequestParam String username) {

        return ingresoMensualService.obtenerResumenAnual(username, anio);
    }
}
