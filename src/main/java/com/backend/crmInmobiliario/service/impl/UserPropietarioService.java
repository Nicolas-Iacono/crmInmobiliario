package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.usuarioInquilino.RegistroInquilinoDto;
import com.backend.crmInmobiliario.DTO.entrada.usuarioPropietario.RegistroPropietarioDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.entity.Propietario;
import com.backend.crmInmobiliario.entity.Role;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.exception.UsernameAlreadyExistsException;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.PropietarioRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.RoleRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IUserPropietarioService;
import com.backend.crmInmobiliario.utils.JsonPrinter;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

@Service
public class UserPropietarioService implements IUserPropietarioService {
    private final Logger LOGGER = LoggerFactory.getLogger(UserInquilinoService.class);
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PropietarioRepository propietarioRepository;
    private ModelMapper modelMapper;

    public UserPropietarioService(ModelMapper modelMapper, PasswordEncoder passwordEncoder, RoleRepository roleRepository, PropietarioRepository propietarioRepository, UsuarioRepository usuarioRepository) {
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.propietarioRepository = propietarioRepository;
        this.usuarioRepository = usuarioRepository;
    }


    @Override
    @Transactional
    public TokenDtoSalida registrarPropietario(RegistroPropietarioDto dto) {

        LOGGER.info("UsuarioEntradaDto: " + JsonPrinter.toString(dto));

        // 1️⃣ Validar existencia previa
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new UsernameAlreadyExistsException("Ya existe una cuenta con este email.");
        }

        // 2️⃣ Buscar propietario existente en BD
        var propietario = propietarioRepository.findByDniOrEmail(dto.getDni(), dto.getEmail())
                .orElseThrow(() -> new RuntimeException("No encontramos tu registro en la inmobiliaria."));

        // 3️⃣ Crear rol si no existe
        Role propietarioRole = roleRepository.findByRol(RolesCostantes.PROPIETARIO_USER)
                .orElseGet(() -> roleRepository.save(new Role(RolesCostantes.PROPIETARIO_USER)));

        // 4️⃣ Crear el usuario del propietario
        Usuario usuarioEntidad = new Usuario();
        usuarioEntidad.setUsername(dto.getEmail());
        usuarioEntidad.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuarioEntidad.setEmail(dto.getEmail());
        usuarioEntidad.setRoles(new HashSet<>(Collections.singleton(propietarioRole)));

        Usuario usuarioPersistido = usuarioRepository.save(usuarioEntidad);


        // 5️⃣ Vincular ambos lados de la relación 🔄
        propietario.setUsuarioCuentaPropietario(usuarioPersistido);

        // 💡 Asegurar que la lista sea mutable
        if (propietario.getPropiedades() == null) {
            propietario.setPropiedades(new ArrayList<>());
        }
        propietarioRepository.save(propietario);

        usuarioPersistido.setPropietario(propietario);
        usuarioRepository.save(usuarioPersistido);

        // 6️⃣ Devolver datos de confirmación
        TokenDtoSalida tokenDto = new TokenDtoSalida();
        tokenDto.setUsername(usuarioPersistido.getUsername());
        tokenDto.setEmail(usuarioPersistido.getEmail());

        return tokenDto;
    }

}
