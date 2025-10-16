package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ImpuestosGeneralSalidaDto {

    private Long id;
    private String tipo;            // Se puede agregar manualmente o derivar del discriminador
    private String descripcion;
    private String empresa;
    private int porcentaje;
    private String numeroCliente;
    private String numeroMedidor;
    private Double montoAPagar;
    private LocalDate fechaFactura;
    private Boolean estadoPago;
    private String urlFactura;


}
