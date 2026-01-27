package com.backend.crmInmobiliario.DTO.salida.contrato;


import java.time.LocalDate;

public record ContractEventDto (

        String id,          // act-11-2026-01-01 | venc-11
        Long contratoId,
        String title,       // 🟠 Actualiza: X | 🔴 Vence: X
        LocalDate start,    // mejor tipo fecha real
        boolean allDay,     // true
        String type    // ACTUALIZA | VENCE
){
}