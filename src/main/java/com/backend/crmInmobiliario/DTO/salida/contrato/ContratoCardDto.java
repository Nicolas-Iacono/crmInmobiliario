package com.backend.crmInmobiliario.DTO.salida.contrato;


import com.backend.crmInmobiliario.DTO.entrada.inquilino.InquilinoContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.PropiedadContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropietarioContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.entity.EstadoContrato;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;


@Data
@NoArgsConstructor
public class ContratoCardDto {

    private Long id;
    private String nombreContrato;
    private Double montoAlquiler;
    private String inquilino;
    private String propietario;
    private String propiedad;
    private Set<EstadoContrato> estados;
    private Boolean activo;

    // ⬇⬇⬇ Constructor que Hibernate necesita
    public ContratoCardDto(
            Long id,
            String nombreContrato,
            Double montoAlquiler,
            String inquilino,
            String propietario,
            String propiedad,
            Set<EstadoContrato> estados,
            Boolean activo

    ) {
        this.id = id;
        this.nombreContrato = nombreContrato;
        this.montoAlquiler = montoAlquiler;
        this.inquilino = inquilino;
        this.propietario = propietario;
        this.propiedad = propiedad;
        this.estados = estados;
        this.activo = activo;

    }
}