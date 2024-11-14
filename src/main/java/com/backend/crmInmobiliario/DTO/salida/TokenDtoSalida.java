package com.backend.crmInmobiliario.DTO.salida;

import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Builder
@JsonPropertyOrder({"username","nombreNegocio","email","roles","jwt","status"})
public class TokenDtoSalida {


    private Long id;
    private String username;
    private String password;
    private String nombreNegocio;
    private String email;
    private Set<ContratoSalidaDto> contratos;
//    private List<Role> roles;
    private Set<GaranteSalidaDto> garantes;
    String message;
    String jwt;
    boolean status;
//    List<Role> roles
    public TokenDtoSalida(Long id, String username, String password, String nombreNegocio, String email, Set<ContratoSalidaDto> contratos, Set<GaranteSalidaDto> garantes, String message, String jwt, boolean status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombreNegocio = nombreNegocio;
        this.email = email;
        this.contratos = contratos;
        this.garantes = garantes;
        this.message = message;
        this.jwt = jwt;
        this.status = status;
    }

    public <E> TokenDtoSalida(Long id, String username, String email, String nombreNegocio, List<Contrato> contratos, List<Inquilino> inquilinos, List<Propietario> propietarios, List<Propiedad> propiedades, List<Garante> garantes, ArrayList<E> es) {
    }
}
