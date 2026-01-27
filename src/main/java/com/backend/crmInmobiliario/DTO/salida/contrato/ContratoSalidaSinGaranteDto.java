package com.backend.crmInmobiliario.DTO.salida.contrato;

import com.backend.crmInmobiliario.DTO.entrada.inquilino.InquilinoContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.PropiedadContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropietarioContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteContratoDtoSalida;
import com.backend.crmInmobiliario.entity.EstadoContrato;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;


@Data
@NoArgsConstructor
public class ContratoSalidaSinGaranteDto {

    private String nombreContrato;
    private Long id;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
    private PropietarioContratoDtoSalida propietario;
    private InquilinoContratoDtoSalida inquilino;
    private PropiedadContratoSalidaDto propiedad;
    private int actualizacion;
    private double montoAlquiler;
    private int duracion;
    private boolean activo;
    private String estado;
    private String tipoGarantia;
    private String aguaEmpresa;
    private int aguaPorcentaje;

    private String luzEmpresa;
    private BigDecimal luzPorcentaje;

    private String gasEmpresa;
    private BigDecimal gasPorcentaje;

    private String municipalEmpresa;
    private BigDecimal municipalPorcentaje;
    //    private ImpuestosGeneralSalidaDto impuestos;
    private List<ReciboSalidaDto> recibos;

    private String indiceAjuste;
    private String montoAlquilerLetras;
    private Double multaXDia;
    private Long tiempoRestante;
    private String destino;

    private String contratoPdf;
    //    private ContratoPdfSalidaDto pdfSalidaDto;
    private UsuarioDtoSalida usuarioDtoSalida;

    private Set<EstadoContrato> estados;
}
