package com.backend.crmInmobiliario.DTO.entrada;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ImpuestoLuzEntradaDto {

    private String descripcion;
    private String empresa;
    private BigDecimal porcentaje;
    private String numeroCliente;
    private BigDecimal montoBase;
    private String numeroMedidor;
    private BigDecimal montoAPagar;
    private LocalDate fechaFactura;
    private UsuarioDtoSalida usuario;
}
