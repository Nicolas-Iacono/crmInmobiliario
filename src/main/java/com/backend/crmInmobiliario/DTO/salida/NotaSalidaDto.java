package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class NotaSalidaDto {

    private Long id;

    private Long idContrato;
    // Blob puede ser texto o imagen. Podrías considerar usar String si solo es texto.
    private String contenido;

    private String motivo;

    private Enum estado; // Ej: "Pendiente", "En Proceso", "Resuelto"

    private String prioridad; // Ej: "Alta", "Media", "Baja"

    private String tipo; // Ej: "Reparación", "Pago", "Contrato"

    private String observaciones; // Campo adicional para comentarios libres

    private LocalDateTime fechaResolucion;

    private LocalDate fechaCreacion;

    private List<ImgUrlSalidaDto> imagenes = new ArrayList<>();

}
