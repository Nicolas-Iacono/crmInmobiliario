package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.usuarioPropietario.RegistroPropietarioDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;

public interface IUserPropietarioService {

    TokenDtoSalida registrarPropietario(RegistroPropietarioDto propietarioDto);
}
