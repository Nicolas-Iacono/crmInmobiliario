package com.backend.crmInmobiliario.DTO.salida.contrato;

import com.backend.crmInmobiliario.DTO.entrada.inquilino.InquilinoContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.*;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ContratoSalidaDto {

    private String nombreContrato;
    private Long id;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
    private PropietarioContratoDtoSalida propietario;
    private InquilinoContratoDtoSalida inquilino;
    private PropiedadContratoSalidaDto propiedad;
    private List<GaranteContratoDtoSalida> garantes;
    private int actualizacion;
    private double montoAlquiler;
    private int duracion;
    private boolean activo;
    private ImpuestosGeneralSalidaDto impuestos;
    private String indiceAjuste;
    private String montoAlquilerLetras;
    private Double multaXDia;
    private Long tiempoRestante;
    private String destino;

    private String contratoPdf;
//    private ContratoPdfSalidaDto pdfSalidaDto;
    private UsuarioDtoSalida usuarioDtoSalida;
}
