package com.backend.crmInmobiliario.DTO.mpDtos.transferencias;

import lombok.Data;

@Data
public class TransferenciaNotificarDto {
    private String canal;      // MERCADOPAGO | BANCO
    private String referencia;
}
