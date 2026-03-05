package com.backend.crmInmobiliario.DTO.entrada.contrato;

import com.backend.crmInmobiliario.entity.ModoRecibos;
import lombok.Data;

@Data
public class ContratoModoRecibosDto {
    private ModoRecibos modoRecibos;
    private Boolean autoRecibosActivo;
    private Integer diaGeneracion;
    private Integer diaVencimiento;
}
