package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.googleOuthApi.GoogleTokenResponseOuth;
import com.backend.crmInmobiliario.DTO.googleOuthApi.GoogleUserInfo;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.entity.UsuarioGoogleAccount;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioGoogleAccountRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@Transactional
public class GoogleLinkService {

    private final UsuarioRepository usuarioRepo;
    private final UsuarioGoogleAccountRepository googleRepo;

    public GoogleLinkService(UsuarioRepository usuarioRepo,
                             UsuarioGoogleAccountRepository googleRepo) {
        this.usuarioRepo = usuarioRepo;
        this.googleRepo = googleRepo;
    }

    /**
     * Overload que recibe los DTOs de Google y delega al método “largo”.
     */
    public UsuarioGoogleAccount linkWithInfo(Long userId,
                                             GoogleUserInfo uinfo,
                                             GoogleTokenResponseOuth tok) {

        Instant exp = tok.expires_in() != null
                ? Instant.now().plusSeconds(tok.expires_in())
                : null;

        return link(
                userId,
                uinfo.sub(),               // googleSub
                uinfo.email(),             // email
                uinfo.name(),              // name
                uinfo.picture(),           // picture
                tok.scope(),               // scope
                tok.access_token(),        // accessToken (opcional)
                exp,                       // accessTokenExp (Instant)
                tok.refresh_token(),       // refreshToken (se guarda sólo si viene)
                Boolean.TRUE.equals(uinfo.email_verified()),
                uinfo.locale()
        );
    }

    /**
     * Método “largo” con todos los campos explícitos.
     * Valida conflicto de sub, crea/actualiza la entidad y persiste.
     */
    public UsuarioGoogleAccount link(Long userId,
                                     String googleSub,
                                     String email,
                                     String name,
                                     String picture,
                                     String scope,
                                     String accessToken,
                                     Instant accessTokenExp,
                                     String refreshTokenEncrypted,
                                     Boolean emailVerified,
                                     String locale) {

        // 1) Usuario debe existir
        Usuario usuario = usuarioRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // 2) Evitar que el mismo sub esté vinculado a otro usuario
        googleRepo.findByGoogleSub(googleSub).ifPresent(existing -> {
            if (!existing.getUsuario().getId().equals(userId)) {
                throw new IllegalStateException("Esta cuenta de Google ya está vinculada a otro usuario");
            }
        });

        // 3) Buscar vínculo existente de este usuario o crear uno nuevo
        UsuarioGoogleAccount uga = googleRepo.findByUsuarioId(userId)
                .orElseGet(UsuarioGoogleAccount::new);

        // 4) Mapear y actualizar
        uga.setUsuario(usuario);
        uga.setGoogleSub(googleSub);
        uga.setEmail(email);
        uga.setName(name);
        uga.setPictureUrl(picture);
        uga.setScope(scope);

        // 4.1) Guardar también los datos básicos en la entidad Usuario
        //      para acceso rápido desde el perfil del usuario.
        usuario.setGoogleId(googleSub);
        usuario.setGoogleEmail(email);

        // Access token (opcional) y expiración
        uga.setAccessToken(accessToken);
        uga.setAccessTokenExpiresAt(accessTokenExp == null ? null : Date.from(accessTokenExp));

        // ⚠️ No pisar el refresh token si Google no envió uno nuevo
        if (refreshTokenEncrypted != null && !refreshTokenEncrypted.isBlank()) {
            uga.setRefreshTokenEncrypted(refreshTokenEncrypted); // tu @Converter lo cifra en BD
        }

        uga.setEmailVerified(emailVerified);
        uga.setLocale(locale);

        if (uga.getLinkedAt() == null) {
            uga.setLinkedAt(new Date());
        }

        // 5) Persistir
        return googleRepo.save(uga);
    }

    /**
     * Desvincular la cuenta de Google del usuario.
     */
    public void unlink(Long userId) {
        googleRepo.findByUsuarioId(userId).ifPresent(uga -> {
            // limpiar referencia en Usuario
            Usuario usuario = uga.getUsuario();
            if (usuario != null) {
                usuario.setGoogleId(null);
                usuario.setGoogleEmail(null);
            }
            googleRepo.delete(uga);
        });
    }

    public boolean isLinked(Long userId) {
        return googleRepo.findByUsuarioId(userId).isPresent();
    }

    /**
     * Obtiene la cuenta de Google vinculada del usuario (o null si no existe).
     */
    public UsuarioGoogleAccount getLinkedAccount(Long userId) {
        return googleRepo.findByUsuarioId(userId).orElse(null);
    }
}
