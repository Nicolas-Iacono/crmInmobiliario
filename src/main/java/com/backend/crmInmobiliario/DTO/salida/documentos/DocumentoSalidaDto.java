package com.backend.crmInmobiliario.DTO.salida.documentos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DocumentoSalidaDto {

    private Long id;
    private String nombreArchivo;
    private String tipo;
    private String urlArchivo;
    private String fechaSubida;

    private Long contratoId;
    private Long inquilinoId;
    private Long propietarioId;
    private Long garanteId;

    private Long usuarioId;
}
