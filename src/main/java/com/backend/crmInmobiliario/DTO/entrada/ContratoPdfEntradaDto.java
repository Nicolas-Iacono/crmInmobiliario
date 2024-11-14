package com.backend.crmInmobiliario.DTO.entrada;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.Contrato;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@NoArgsConstructor
public class ContratoPdfEntradaDto {

    private String paragraph;
    private Long contrato_id;

}
