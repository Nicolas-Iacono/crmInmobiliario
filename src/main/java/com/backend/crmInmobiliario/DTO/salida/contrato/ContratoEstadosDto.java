package com.backend.crmInmobiliario.DTO.salida.contrato;

import com.backend.crmInmobiliario.entity.EstadoContrato;

import java.util.Set;

public record ContratoEstadosDto(Long idContrato, Set<EstadoContrato> estados) {
}
