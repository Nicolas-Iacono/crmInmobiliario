package com.backend.crmInmobiliario.DTO.entrada;


import com.backend.crmInmobiliario.entity.EstadoNota;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Blob;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NotaEntradaDto {

    private Long idContrato;
    // Blob puede ser texto o imagen. Podrías considerar usar String si solo es texto.

    private String contenido;

    private String motivo;

    private EstadoNota estado; // Ej: "Pendiente", "En Proceso", "Resuelto"

    private String prioridad; // Ej: "Alta", "Media", "Baja"

    private String tipo; // Ej: "Reparación", "Pago", "Contrato"

    private String observaciones; // Campo adicional para comentarios libres



}
