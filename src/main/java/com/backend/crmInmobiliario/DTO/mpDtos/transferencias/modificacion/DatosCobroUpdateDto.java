package com.backend.crmInmobiliario.DTO.mpDtos.transferencias.modificacion;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DatosCobroUpdateDto {

    @Size(max = 60)
    private String alias;

    @Size(max = 30)
    private String cbu;

    @Size(max = 120)
    private String titular;

    @Size(max = 20)
    private String cuit;

    @Size(max = 80)
    private String banco;
}
