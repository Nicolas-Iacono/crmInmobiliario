package com.backend.crmInmobiliario.utils;

import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String message;
    private T data;

    public ApiResponse(boolean b, String contratoActualizadoConÉxito, ContratoSalidaDto contratoActualizado) {
    }

    public ApiResponse(boolean b, String reciboCreadoCorrectamente, ReciboSalidaDto reciboSalidaDto) {
    }
}
