package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Primary
public class UserDetailService implements UserDetailsService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Busca el usuario en la base de datos por su username
        Usuario usuario = usuarioRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario " + username + " no existe"));

        // Crea una lista de autoridades (roles y permisos)
        List<SimpleGrantedAuthority> listaDeAutoridades = new ArrayList<>();

        // Agrega los roles con el prefijo "ROLE_"
        usuario.getRoles().forEach(role ->
                listaDeAutoridades.add(new SimpleGrantedAuthority("ROLE_" + role.getRol())));

        // Agrega los permisos de cada rol (si existen)
        usuario.getRoles().stream()
                .flatMap(role -> role.getPermisosList().stream())
                .forEach(permiso ->
                        listaDeAutoridades.add(new SimpleGrantedAuthority(permiso.getName())));

        // Retorna un objeto CustomUserDetails con las autoridades configuradas
        return new CustomUserDetails(usuario);
    }
}
