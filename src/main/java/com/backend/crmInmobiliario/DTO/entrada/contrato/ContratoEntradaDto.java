package com.backend.crmInmobiliario.DTO.entrada.contrato;


import com.backend.crmInmobiliario.utils.CustomLocalDateDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class ContratoEntradaDto {

    @NotNull(message = "El contrato debe tener un nombre")
    private String nombreContrato;
    @NotNull(message = "fecha inicio no puede ser nulo")
    @JsonDeserialize(using = CustomLocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate fecha_inicio;

    @NotNull(message = "fecha fin no puede ser nulo")
    @JsonDeserialize(using = CustomLocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fecha_fin;

    @NotNull(message = "debe asignarse un propietario")
    private Long id_propietario;

    @NotNull(message = "debe asignarse un inquilino")
    private Long id_inquilino;

    @NotNull(message = "debe asignarse una propiedad")
    private Long id_propiedad;

    private List<Long> garantesIds;

    private String aguaEmpresa;
    private int aguaPorcentaje;

    private String luzEmpresa;
    private int luzPorcentaje;

    private String gasEmpresa;
    private int gasPorcentaje;

    private String municipalEmpresa;
    private int municipalPorcentaje;

    private int actualizacion;
    private Double montoAlquiler;

    private String indiceAjuste;
    private String montoAlquilerLetras;
    private Double multaXDia;

    private int duracion;
    private String destino;

    private String nombreUsuario;
}
