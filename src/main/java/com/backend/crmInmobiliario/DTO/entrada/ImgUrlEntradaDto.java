package com.backend.crmInmobiliario.DTO.entrada;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImgUrlEntradaDto {
    @NotNull(message = "El id del inquilino es obligatorio")
    private Long inquilino_id;

    @NotBlank(message = "La URL de la imagen no puede estar vacía")
    @Size(max = 1024, message = "La longitud máxima de la URL de la imagen es de {max} caracteres")
    private String imageUrl;

}
