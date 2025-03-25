package com.backend.crmInmobiliario.entity.impuestos;

import com.backend.crmInmobiliario.entity.Impuesto;
import com.backend.crmInmobiliario.entity.Recibo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@DiscriminatorValue("MUNICIPAL")
@Entity
@NoArgsConstructor
@Table(name = "municipal")
@Inheritance(strategy = InheritanceType.JOINED)
public class Municipal extends Impuesto {


}
