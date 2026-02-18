package com.backend.crmInmobiliario.DTO.entrada.propiedades;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PropiedadEntradaDto {

    @NotNull(message = "La dirección no puede ser nula")
    private String direccion;

    @NotNull(message = "La localidad no puede ser nula")
    private String localidad;

    @NotNull(message = "El partido no puede ser nulo")
    private String partido;

    @NotNull(message = "La provincia no puede ser nula")
    private String provincia;
    private Double precio;
    private Boolean disponibilidad;

    private Integer cantidadAmbientes;
    private Boolean pileta;
    private Boolean cochera;
    private Boolean jardin;
    private Boolean patio;
    private Boolean balcon;

    private boolean visibleAOtros;

    private Long id_propietario;

    private String inventario;
    private String tipo;

}
