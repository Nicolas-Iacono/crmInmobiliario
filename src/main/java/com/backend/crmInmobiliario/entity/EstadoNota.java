package com.backend.crmInmobiliario.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;



public enum EstadoNota {
        PENDIENTE,
        EN_PROCESO,
        RESUELTO,
        CANCELADO
}
