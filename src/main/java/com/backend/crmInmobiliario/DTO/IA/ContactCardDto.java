package com.backend.crmInmobiliario.DTO.IA;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactCardDto {
    private String tipo;            // "inquilino", "propietario", "garante"
    private Long id;
    private String nombreCompleto;
    private String telefono;
    private String email;

}
