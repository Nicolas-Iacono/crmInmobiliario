package com.backend.crmInmobiliario.entity.impuestos;

import com.backend.crmInmobiliario.entity.Impuesto;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DiscriminatorValue("DEUDA_PENDIENTE")
@Entity
@NoArgsConstructor
@Table(name = "deudas_pendientes")
public class DeudaPendiente extends Impuesto {
}
