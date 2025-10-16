package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.UserAdminEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.usuarioInquilino.RegistroInquilinoDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.entity.Role;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.UsernameAlreadyExistsException;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.RoleRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IUserInquilinoService;
import com.backend.crmInmobiliario.utils.JsonPrinter;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;


@Service
public class UserInquilinoService implements IUserInquilinoService {
    private final Logger LOGGER = LoggerFactory.getLogger(UserInquilinoService.class);
    private final UsuarioRepository usuarioRepository;
    private final InquilinoRepository inquilinoRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private ModelMapper modelMapper;

    public UserInquilinoService(UsuarioRepository usuarioRepository, InquilinoRepository inquilinoRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.usuarioRepository = usuarioRepository;
        this.inquilinoRepository = inquilinoRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        configureMapping();
    }

    private void configureMapping() {
        modelMapper.typeMap(RegistroInquilinoDto.class, Usuario.class)
                .addMappings(mapper -> mapper.map(RegistroInquilinoDto::getNombre, Usuario::setUsername));
        modelMapper.typeMap(Usuario.class, TokenDtoSalida.class)
                .addMappings(mapper -> mapper.map(Usuario::getUsername, TokenDtoSalida::setUsername));
    }

    @Override
    @Transactional
    public TokenDtoSalida registrarInquilino(RegistroInquilinoDto dto) {

        LOGGER.info("UsuarioEntradaDto: " + JsonPrinter.toString(dto));

        // 1️⃣ Validar existencia previa
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new UsernameAlreadyExistsException("Ya existe una cuenta con este email.");
        }

        // 2️⃣ Buscar inquilino en BD
        var inquilino = inquilinoRepository.findByDniOrEmail(dto.getDni(), dto.getEmail())
                .orElseThrow(() -> new RuntimeException("No encontramos tu registro en la inmobiliaria."));

        // 3️⃣ Crear rol si no existe
        Role inquilinoRole = roleRepository.findByRol(RolesCostantes.INQUILINO_USER)
                .orElseGet(() -> roleRepository.save(new Role(RolesCostantes.INQUILINO_USER)));

        // 4️⃣ Crear el usuario del inquilino
        Usuario usuarioEntidad = new Usuario();
        usuarioEntidad.setUsername(dto.getEmail());
        usuarioEntidad.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuarioEntidad.setEmail(dto.getEmail());
        usuarioEntidad.setRoles(Collections.singleton(inquilinoRole));

        Usuario usuarioPersistido = usuarioRepository.save(usuarioEntidad);

        // 5️⃣ Vincular el inquilino al nuevo usuarioCuenta
        inquilino.setUsuarioCuentaInquilino(usuarioPersistido);
        inquilinoRepository.save(inquilino);

        // 6️⃣ Devolver datos de confirmación
        TokenDtoSalida tokenDto = new TokenDtoSalida();
        tokenDto.setUsername(usuarioPersistido.getUsername());
        tokenDto.setEmail(usuarioPersistido.getEmail());

        return tokenDto;
    }
}