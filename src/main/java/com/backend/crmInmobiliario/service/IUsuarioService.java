package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.AuthResponse;
import com.backend.crmInmobiliario.DTO.entrada.LoginEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.UserAdminEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface IUsuarioService {


    List<UsuarioDtoSalida> listarUsuarios();

    TokenDtoSalida registrarUsuarioAdmin(UserAdminEntradaDto admin);

//    TokenSalidaDto createUser(UserDTO usuario) throws DataIntegrityViolationException;
//
//    TokenSalidaDto createUserAdmin(UserAdminEntradaDto userAdminEntradaDto) throws DataIntegrityViolationException;

    UsuarioDtoSalida buscarUsuarioPorId(Long id) throws IOException, ResourceNotFoundException;
    UsuarioDtoSalida buscarUsuarioPorUsername(String username) throws ResourceNotFoundException;

    void eliminarUsuario(Long id);

//    UsuarioDtoSalida actualizarUsuario(UserModificacionEntradaDTO usuario);
//
    UsuarioDtoSalida buscarUsuarioPorEmail(String email);

//    TokenDtoSalida registrarAdmin(UserAdminEntradaDto userAdminEntradaDto);

    AuthResponse loginUser(LoginEntradaDto loginEntradaDto);


}
