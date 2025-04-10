package com.backend.crmInmobiliario.entity.impuestos;


import com.backend.crmInmobiliario.entity.Impuesto;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DiscriminatorValue("EXP_EXT_ORD")
@Entity
@NoArgsConstructor
@Table(name = "expensas_extra_ordinarias")
public class ExpensaExtraOrdinaria extends Impuesto {
}
