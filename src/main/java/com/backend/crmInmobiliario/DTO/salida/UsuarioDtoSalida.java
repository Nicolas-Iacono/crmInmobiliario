package com.backend.crmInmobiliario.DTO.salida;

//import com.backend.crmInmobiliario.entity.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class UsuarioDtoSalida {

    private Long id;
    private String username;
    private String password;
    private String nombreNegocio;
    private String email;

//    private List<Role> roles;

}
