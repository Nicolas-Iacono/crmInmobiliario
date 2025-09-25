package com.backend.crmInmobiliario.DTO.entrada;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PresupuestoEntradaDto {
    private Long usuarioId;         // id del usuario (en la entidad puede ser User/Usuario)
    private String titulo;
    private Double monto;           // alquiler mensual base
    private String porcentajeContrato; // ej "3.5"
    private String porcentajeSello;    // ej "1.2"
    private int duracion;              // meses
    private Double gastosExtras;       // opcional
    private String nombreUsuario;
}