package com.backend.crmInmobiliario.DTO.salida.contrato;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratoActualizacionDtoSalida {

    private LocalDate fechaProximaActualizacion;
    private int mesesRestantes;
    private int diasRestantes;
    private boolean vencido;
    private String mensaje;

}
