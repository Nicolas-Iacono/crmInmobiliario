package com.backend.crmInmobiliario.DTO.salida;

import com.backend.crmInmobiliario.entity.Contrato;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@NoArgsConstructor
public class ContratoPdfSalidaDto {

    private Long id_pdfContrato;

    private String paragraph;

    private Date fechaCreacion;

}
