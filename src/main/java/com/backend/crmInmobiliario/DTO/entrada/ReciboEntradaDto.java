package com.backend.crmInmobiliario.DTO.entrada;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.impuestos.Agua;
import com.backend.crmInmobiliario.entity.impuestos.Gas;
import com.backend.crmInmobiliario.entity.impuestos.Luz;
import com.backend.crmInmobiliario.entity.impuestos.Municipal;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@NoArgsConstructor
public class ReciboEntradaDto {


    private Long idContrato;        // ID del contrato relacionado con el recibo

    private LocalDate periodo;      // Periodo del recibo

    private BigDecimal montoTotal;  // Monto total a pagar

    // Información de los impuestos o servicios
    private Double aguaServicio;  // Monto del servicio de agua
    private Double luzServicio;   // Monto del servicio de luz
    private Double gasServicio;   // Monto del servicio de gas
    private Double municipalServicio;     // Monto del impuesto municipal
    private String nombreUsuario;
}
