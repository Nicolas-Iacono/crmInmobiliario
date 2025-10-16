package com.backend.crmInmobiliario.DTO.salida.propietario;

//import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.propiedad.PropiedadSalidaDtoNoRecursivo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PropietarioSalidaDto {
    private Long id;
    private String pronombre;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String dni;
    private String direccionResidencial;
    private String cuit;
    private String nacionalidad;
    private String estadoCivil;
//    private List<ImgUrlSalidaDto> imagenes = new ArrayList<>();
    // Lista de propiedades del propietario
    private UsuarioDtoSalida usuarioDtoSalida;
}
