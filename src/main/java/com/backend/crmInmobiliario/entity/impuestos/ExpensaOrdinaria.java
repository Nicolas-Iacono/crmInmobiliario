package com.backend.crmInmobiliario.entity.impuestos;

import com.backend.crmInmobiliario.entity.Impuesto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DiscriminatorValue("EXP_ORD")
@Entity
@NoArgsConstructor
@Table(name = "expensas_ordinarias")
@Inheritance(strategy = InheritanceType.JOINED)
public class ExpensaOrdinaria extends Impuesto {
}
