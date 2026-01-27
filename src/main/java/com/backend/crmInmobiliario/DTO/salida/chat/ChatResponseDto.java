package com.backend.crmInmobiliario.DTO.salida.chat;

import com.backend.crmInmobiliario.DTO.IA.ContactCardDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDto {
    private String respuesta;      // texto que vas a mostrar igual
    private String modo;           // "mixto", "datos", etc.
    private ContactCardDto contacto;
}
