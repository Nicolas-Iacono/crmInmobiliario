package com.backend.crmInmobiliario.DTO.salida;

import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoIdSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.LatestContratosSalidaDto;
import com.backend.crmInmobiliario.entity.TransferStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ReciboSalidaDto {

    private Long id;
    private Long contratoId;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String periodo;
    private String concepto;
    private BigDecimal montoTotal;
    private int numeroRecibo;
    private Boolean estado;
    private String nombreContrato;
    private TransferStatus transferStatus;

    // Lista de impuestos asociados, ya convertidos a DTO de salida
    private List<ImpuestosGeneralSalidaDto> impuestos;




}
