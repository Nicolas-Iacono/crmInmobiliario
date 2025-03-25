package com.backend.crmInmobiliario.DTO.modificacion;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoEntradaDto;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@NoArgsConstructor
public class ReciboModificacionDto {
    private Long id;
    private Long idContrato;       // Identificador del contrato asociado
    private String periodo;        // Periodo al que corresponde el recibo
    private String concepto;       // Concepto o descripción del recibo
    private BigDecimal montoTotal; // Monto total del recibo

    // Lista de impuestos asociados
    @Valid
    private List<ImpuestoEntradaDto> impuestos;
    private Boolean estado;
}
