package com.backend.crmInmobiliario.DTO.salida.inquilino;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquilinoUser {
    private String username;
    private String password;
}
