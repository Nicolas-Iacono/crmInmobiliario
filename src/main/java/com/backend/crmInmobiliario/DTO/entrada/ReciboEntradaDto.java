package com.backend.crmInmobiliario.DTO.entrada;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.impuestos.Agua;
import com.backend.crmInmobiliario.entity.impuestos.Gas;
import com.backend.crmInmobiliario.entity.impuestos.Luz;
import com.backend.crmInmobiliario.entity.impuestos.Municipal;
import jakarta.persistence.ManyToOne;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ReciboEntradaDto {


    private Long idContrato;       // Identificador del contrato asociado
    private int numeroRecibo;
    private String periodo;        // Periodo al que corresponde el recibo
    private String concepto;       // Concepto o descripción del recibo
    private BigDecimal montoTotal; // Monto total del recibo

    // Lista de impuestos asociados
    @Valid
    private List<ImpuestoEntradaDto> impuestos;
    private Boolean estado;
}
