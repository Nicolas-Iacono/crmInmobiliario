package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.usuarioGarante.RegistroGaranteDto;
import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.backend.crmInmobiliario.entity.Role;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.UsernameAlreadyExistsException;
import com.backend.crmInmobiliario.repository.GaranteRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.RoleRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IUserGaranteService;
import com.backend.crmInmobiliario.utils.JsonPrinter;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;

@Service
public class UserGaranteService implements IUserGaranteService {

    private final Logger LOGGER = LoggerFactory.getLogger(UserGaranteService.class);
    private final UsuarioRepository usuarioRepository;
    private final GaranteRepository garanteRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserGaranteService(UsuarioRepository usuarioRepository,
                              GaranteRepository garanteRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.garanteRepository = garanteRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public TokenDtoSalida registrarGarante(RegistroGaranteDto dto) {
        LOGGER.info("RegistroGaranteDto: {}", JsonPrinter.toString(dto));

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new UsernameAlreadyExistsException("Ya existe una cuenta con este email.");
        }

        var garante = garanteRepository.findByDniOrEmail(dto.getDni(), dto.getEmail())
                .orElseThrow(() -> new RuntimeException("No encontramos tu registro en la inmobiliaria."));

        Role garanteRole = roleRepository.findByRol(RolesCostantes.GARANTE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RolesCostantes.GARANTE_USER)));

        Usuario usuarioEntidad = new Usuario();
        usuarioEntidad.setUsername(dto.getEmail());
        usuarioEntidad.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuarioEntidad.setEmail(dto.getEmail());
        usuarioEntidad.setRoles(new HashSet<>(Collections.singleton(garanteRole)));

        Usuario usuarioPersistido = usuarioRepository.save(usuarioEntidad);

        garante.setUsuarioCuentaGarante(usuarioPersistido);
        garanteRepository.save(garante);

        usuarioPersistido.setGarante(garante);
        usuarioRepository.save(usuarioPersistido);

        TokenDtoSalida tokenDto = new TokenDtoSalida();
        tokenDto.setUsername(usuarioPersistido.getUsername());
        tokenDto.setEmail(usuarioPersistido.getEmail());

        return tokenDto;
    }
}
