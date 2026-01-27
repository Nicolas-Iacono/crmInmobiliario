package com.backend.crmInmobiliario.DTO.salida.contrato;

import com.backend.crmInmobiliario.DTO.entrada.inquilino.InquilinoContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.PropiedadContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropietarioContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratoPdfDto {

    private String nombreContrato;
    private Long id;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
    private PropietarioContratoDtoSalida propietario;
    private InquilinoContratoDtoSalida inquilino;
    private PropiedadContratoSalidaDto propiedad;
    private List<GaranteSalidaDto> garantes;
    private int actualizacion;
    private Double montoAlquiler;
    private int duracion;
    private boolean activo;

    private String aguaEmpresa;
    private BigDecimal aguaPorcentaje;

    private String luzEmpresa;
    private BigDecimal luzPorcentaje;

    private String gasEmpresa;
    private BigDecimal gasPorcentaje;

    private String municipalEmpresa;
    private BigDecimal municipalPorcentaje;

    private String indiceAjuste;
    private String montoAlquilerLetras;
    private Double multaXDia;
    private Long tiempoRestante;
    private String destino;
    private String tipoGarantia;

    private String contratoPdf;
    private UsuarioDtoSalida usuarioDtoSalida;
}
