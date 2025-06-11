package com.backend.crmInmobiliario.DTO.modificacion;

import com.backend.crmInmobiliario.entity.EstadoNota;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Blob;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NotaModificacionDto {

    private Long id;

    private Long idContrato;
    // Blob puede ser texto o imagen. Podrías considerar usar String si solo es texto.

    private EstadoNota estado; // Ej: "Pendiente", "En Proceso", "Resuelto"

    private String prioridad; // Ej: "Alta", "Media", "Baja"

    private LocalDateTime fechaResolucion;
}
