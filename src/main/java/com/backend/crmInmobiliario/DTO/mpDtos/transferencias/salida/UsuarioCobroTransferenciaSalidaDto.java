package com.backend.crmInmobiliario.DTO.mpDtos.transferencias.salida;

import lombok.Data;

@Data
public class UsuarioCobroTransferenciaSalidaDto {
    private String alias;
    private String cbu;
    private String titular;
    private String banco;
}
