package com.backend.crmInmobiliario.DTO.entrada.documentos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DocumentoEntradaDto {
    private String tipo; // "CONTRATO_PDF" | "RECIBO_SUELDO" | DNI | ETC
    private Long contratoId;
    private Long inquilinoId;
    private Long propietarioId;
    private Long garanteId;
    private String nombreArchivo;
}
