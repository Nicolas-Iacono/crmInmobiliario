package com.backend.crmInmobiliario.DTO.salida;

//import com.backend.crmInmobiliario.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioDtoSalida {

    private Long id;
    private String username;
    private String password;
    private String nombreNegocio;
    private String email;
    private String logo;
    private String matricula;
    private String razonSocial;
    private String localidad;
    private String partido;
    private String provincia;
    private String cuit;
    private String telefono;
}
