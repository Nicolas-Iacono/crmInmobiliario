package com.backend.crmInmobiliario.DTO.salida.recibo;

import com.backend.crmInmobiliario.DTO.salida.ImpuestosGeneralSalidaDto;
import com.backend.crmInmobiliario.entity.TransferStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Data
@NoArgsConstructor
public class LatestRecibosSalidaDto {

    private Long id;
    private Long contratoId;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String periodo;
    private BigDecimal montoTotal;
    private int numeroRecibo;
    private Boolean estado;
    private String nombreContrato;
    private TransferStatus transferStatus;

}
