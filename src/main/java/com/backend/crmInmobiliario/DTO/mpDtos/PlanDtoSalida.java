package com.backend.crmInmobiliario.DTO.mpDtos;


import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class PlanDtoSalida {

    private String id; // Este es el ID del plan que debemos guardar como externalPlanId
    private String reason;
    private String status;

    @JsonProperty("back_url")
    private String backUrl;

    @JsonProperty("init_point")
    private String initPoint;
}
