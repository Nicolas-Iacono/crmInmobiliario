package com.backend.crmInmobiliario.entity.impuestos;

import com.backend.crmInmobiliario.entity.Impuesto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@DiscriminatorValue("OTRO")
@Entity
@NoArgsConstructor
@Table(name = "otros")
@Inheritance(strategy = InheritanceType.JOINED)
public class Otro extends Impuesto {
}
