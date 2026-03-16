package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.usuarioGarante.RegistroGaranteDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;

public interface IUserGaranteService {

    TokenDtoSalida registrarGarante(RegistroGaranteDto garanteDto);
}
