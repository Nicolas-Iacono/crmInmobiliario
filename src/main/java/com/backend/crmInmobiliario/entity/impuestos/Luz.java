package com.backend.crmInmobiliario.entity.impuestos;

import com.backend.crmInmobiliario.entity.Impuesto;
import com.backend.crmInmobiliario.entity.Recibo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@DiscriminatorValue("LUZ")
@Entity
@NoArgsConstructor
@Table(name = "luz")
@Inheritance(strategy = InheritanceType.JOINED)
public class Luz extends Impuesto {


}
