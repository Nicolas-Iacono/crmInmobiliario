package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.PlantillaContratoDtoEntrada;
import com.backend.crmInmobiliario.DTO.salida.PlantillaContratoDtoSalida;
import com.backend.crmInmobiliario.entity.PlantillaContrato;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.service.impl.PlantillaContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plantillas")
@CrossOrigin(origins = {"http://localhost:3000", "https://tuinmo.net"}, allowCredentials = "true")
public class PlantillaContratoController {

    private final PlantillaContratoService plantillaService;

    /**
     * 🔹 Crear nueva plantilla
     */
    @PostMapping("/crear")
    public ResponseEntity<PlantillaContratoDtoSalida> crearPlantilla(
            @RequestBody PlantillaContratoDtoEntrada dto) {
        return ResponseEntity.ok(plantillaService.crearPlantilla(dto));
    }

    /**
     * 🔹 Listar todas las plantillas de una inmobiliaria
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PlantillaContratoDtoSalida>> listarPlantillas(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(plantillaService.listarPlantillas(usuarioId));
    }

    /**
     * 🔹 Obtener una plantilla específica
     */
    @GetMapping("/{plantillaId}/usuario/{usuarioId}")
    public ResponseEntity<PlantillaContratoDtoSalida> obtenerPlantilla(
            @PathVariable Long plantillaId,
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(plantillaService.obtenerPlantillaPorId(usuarioId, plantillaId));
    }

    /**
     * 🔹 Eliminar una plantilla
     */
    @DeleteMapping("/{plantillaId}/usuario/{usuarioId}")
    public ResponseEntity<Void> eliminarPlantilla(
            @PathVariable Long plantillaId,
            @PathVariable Long usuarioId) {
        plantillaService.eliminarPlantilla(usuarioId, plantillaId);
        return ResponseEntity.noContent().build();
    }
}