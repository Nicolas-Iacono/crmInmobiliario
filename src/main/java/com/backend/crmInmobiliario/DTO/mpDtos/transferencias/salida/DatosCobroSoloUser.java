package com.backend.crmInmobiliario.DTO.mpDtos.transferencias.salida;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DatosCobroSoloUser {

    private String alias;
    private String cbu;
    private String titular;
    private String cuit;
    private String banco;

    public DatosCobroSoloUser(String alias, String cbu, String titular, String cuit, String banco) {
        this.alias = alias;
        this.cbu = cbu;
        this.titular = titular;
        this.cuit = cuit;
        this.banco = banco;
    }
}
