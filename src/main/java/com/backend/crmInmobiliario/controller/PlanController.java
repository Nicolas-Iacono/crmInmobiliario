package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.mpDtos.PlanDtoEntrada;
import com.backend.crmInmobiliario.DTO.mpDtos.PlanDtoSalida;

import com.backend.crmInmobiliario.service.impl.PlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de Planes de Suscripción.
 * Expone endpoints para la creación de planes en Mercado Pago y su registro local.
 */
@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    /**
     * Endpoint para crear un nuevo Plan de Suscripción en Mercado Pago y guardarlo localmente.
     * * Recibe la configuración del plan (precio, frecuencia, moneda) en el cuerpo JSON,
     * y los metadatos internos del plan (código, nombre, límite) como Query Parameters.
     * * @param planDto DTO con los detalles de la recurrencia y configuración de Mercado Pago.
     * @param planCode Código único del plan (e.g., "PRO", "PLUS").
     * @param planName Nombre descriptivo del plan.
     * @param contractLimit Límite de contratos que permite este plan.
     * @return Respuesta con el PlanDtoSalida de Mercado Pago y estado HTTP 201.
     */
    @PostMapping("/mp")
    public ResponseEntity<PlanDtoSalida> createMpPlan(
            @RequestBody PlanDtoEntrada planDto,
            @RequestParam String planCode,
            @RequestParam String planName,
            @RequestParam Integer contractLimit) {

        try {
            PlanDtoSalida response = planService.createPlan(planDto, planCode, planName, contractLimit);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Maneja errores de validación de campos obligatorios
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            // Maneja errores de la comunicación con MP o errores internos del servicio
            // Aquí puedes loggear el error completo
            System.err.println("Error en PlanController: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
