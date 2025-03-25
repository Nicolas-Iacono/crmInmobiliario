package com.backend.crmInmobiliario.DTO.entrada;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ImpuestoLuzEntradaDto {

    private String descripcion;
    private String empresa;
    private int porcentaje;
    private String numeroCliente;
    private String numeroMedidor;
    private Double montoAPagar;
    private LocalDate fechaFactura;
    private UsuarioDtoSalida usuario;
}
