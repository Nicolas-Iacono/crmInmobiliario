package com.backend.crmInmobiliario.DTO.entrada.oficios;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OficioImagenPerfilEmpresaEntradaDto {

    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String imagenUrl;
}
