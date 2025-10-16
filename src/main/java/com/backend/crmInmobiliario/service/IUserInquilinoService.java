package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.UserAdminEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.usuarioInquilino.RegistroInquilinoDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IUserInquilinoService {


    TokenDtoSalida registrarInquilino(RegistroInquilinoDto inquilinoDto);
}
