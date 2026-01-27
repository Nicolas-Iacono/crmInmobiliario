package com.backend.crmInmobiliario.DTO.IA;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextItem {

    // 👇 Acepta tanto "id" como "id_contrato"
    @JsonProperty("id")
    private Long id;

    @JsonProperty("id_contrato")
    private Long idContrato;
    private String tipo;
    private String contenido;
    private Double score;
    private Double similaridad;
}

