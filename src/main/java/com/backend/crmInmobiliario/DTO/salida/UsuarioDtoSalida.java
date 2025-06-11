package com.backend.crmInmobiliario.DTO.salida;

//import com.backend.crmInmobiliario.entity.Role;
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
//    private List<Role> roles;

}
