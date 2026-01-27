package com.backend.crmInmobiliario.DTO.salida.contrato;

import com.backend.crmInmobiliario.DTO.entrada.inquilino.InquilinoContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.*;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
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
@AllArgsConstructor
public class ContratoSalidaDto {

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
    private String estado;
    private String aguaEmpresa;
    private BigDecimal aguaPorcentaje;

    private String luzEmpresa;
    private BigDecimal luzPorcentaje;

    private String gasEmpresa;
    private BigDecimal gasPorcentaje;

    private String municipalEmpresa;
    private BigDecimal municipalPorcentaje;
    private List<ReciboSalidaDto> recibos;

    private String indiceAjuste;
    private String montoAlquilerLetras;
    private Double multaXDia;
    private Long tiempoRestante;
    private String destino;
    private String tipoGarantia;

    private String contratoPdf;
    private UsuarioDtoSalida usuarioDtoSalida;
    /** % comisión por contrato (una sola vez al firmar) */
    private BigDecimal comisionContratoPorc;
    /** % comisión mensual */
    private BigDecimal comisionMensualPorc;

    /** monto = alquiler * meses * (comisionContratoPorc/100) */
    private BigDecimal comisionContratoMonto;
    /** monto = alquiler * (comisionMensualPorc/100) */
    private BigDecimal comisionMensualMonto;
    /** monto = alquiler - comisionMensualMonto */
    private BigDecimal montoMensualPropietario;
    private Set<EstadoContrato> estados;
    private boolean vencido;



}
