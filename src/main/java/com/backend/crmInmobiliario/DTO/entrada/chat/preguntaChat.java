package com.backend.crmInmobiliario.DTO.entrada.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class preguntaChat {
    private String username;
    private String pregunta;
}
