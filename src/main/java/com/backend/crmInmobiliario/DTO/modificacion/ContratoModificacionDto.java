package com.backend.crmInmobiliario.DTO.modificacion;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContratoModificacionDto {
    private Long idContrato;
    private String pdfContratoTexto;
}
