package com.backend.crmInmobiliario.DTO.entrada;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ImpuestoEntradaDto {

    @NotNull(message = "El tipo de impuesto es obligatorio")
    private String tipoImpuesto;
    private String descripcion;
    private String Empresa;
    private BigDecimal porcentaje;
    private BigDecimal montoBase;
    private String numeroCliente;
    private String numeroMedidor;
    @NotNull(message = "El monto del impuesto es obligatorio")
    private BigDecimal montoAPagar;
    private LocalDate fechaFactura;
    private Boolean estadoPago;
    private MultipartFile archivoFactura;

}
