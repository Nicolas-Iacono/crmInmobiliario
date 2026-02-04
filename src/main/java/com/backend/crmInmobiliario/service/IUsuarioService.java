package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.AuthResponse;
import com.backend.crmInmobiliario.DTO.entrada.LoginEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.UserAdminEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.usuarioInquilino.LoginInquilinoEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.usuarioPropietario.LoginPropietarioEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ActualizarUsuarioDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.entrada.UsuarioCobroTransferenciaDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.modificacion.DatosCobroUpdateDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.salida.DatosCobroSoloUser;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface IUsuarioService extends UserDetailsService  {

    List<UsuarioDtoSalida> listarUsuarios();

    TokenDtoSalida registrarUsuarioAdmin(UserAdminEntradaDto admin);

    TokenDtoSalida registrarUsuario(UserAdminEntradaDto usuario );

    TokenDtoSalida registrarUsuarioSuperAdmin(UserAdminEntradaDto superAdmin );


    UsuarioDtoSalida buscarUsuarioPorId(Long id) throws IOException, ResourceNotFoundException;

    UsuarioDtoSalida buscarUsuarioPorNombreNegocio(String nombreNegocio) throws ResourceNotFoundException;

    void eliminarUsuario(Long id);

//    UsuarioDtoSalida actualizarUsuario(UserModificacionEntradaDTO usuario);
//
    UsuarioDtoSalida buscarUsuarioPorEmail(String email);

//    TokenDtoSalida registrarAdmin(UserAdminEntradaDto userAdminEntradaDto);

    AuthResponse loginUser(LoginEntradaDto loginEntradaDto);

    UsuarioDtoSalida obtenerUsuarioPorIdDesdeToken(Long userId);

    AuthResponse loginInquilino(LoginInquilinoEntradaDto loginInquilinoEntradaDto);

    AuthResponse loginPropietario(LoginPropietarioEntradaDto loginPropietarioEntradaDto);

    UsuarioDtoSalida actualizarUsuario(Long id, ActualizarUsuarioDto dto) throws ResourceNotFoundException;

    boolean deleteAccountByNombreNegocio(String nombreNegocio);

    void guardarDatosCobroTransferencia(
            Long usuarioId,
            UsuarioCobroTransferenciaDto dto
    );

    DatosCobroSoloUser listarDatosBancariosUser(Long userId);

    DatosCobroSoloUser editarDatosBancariosUser(Long userId, DatosCobroUpdateDto dto);

}
