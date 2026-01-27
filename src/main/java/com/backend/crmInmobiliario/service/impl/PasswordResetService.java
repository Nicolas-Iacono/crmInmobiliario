package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.entity.PasswordResetToken;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.PasswordResetTokenRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UsuarioRepository usuarioRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url:https://tuinmo.net}")
    private String frontendUrl;

    @Value("${app.reset-token-expiration-minutes:60}")
    private long tokenExpiryMinutes;

    /**
     * Genera y envía el token de recuperación.
     */
    public void createAndSendToken(String email) {
        Optional<Usuario> optionalUser = usuarioRepo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            log.warn("Solicitud de recuperación para email inexistente: {}", email);
            return; // seguridad: no revelar si existe
        }

        Usuario user = optionalUser.get();

        // invalidar tokens anteriores
        tokenRepo.findAllByUserAndUsedFalse(user).forEach(t -> {
            t.setUsed(true);
            tokenRepo.save(t);
        });

        String rawToken = generateToken();
        String tokenHash = sha256Hex(rawToken);

        PasswordResetToken prt = new PasswordResetToken();
        prt.setUser(user);
        prt.setTokenHash(tokenHash);
        prt.setCreatedAt(LocalDateTime.now());
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(tokenExpiryMinutes));
        prt.setUsed(false);
        tokenRepo.save(prt);

        String resetLink = frontendUrl + "/reset-password?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8)
                + "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);

        sendResetEmail(user.getEmail(), resetLink);
        log.info("Token de recuperación generado y enviado a {}", email);
    }

    /**
     * Cambia la contraseña si el token es válido.
     */
    public void resetPassword(String email, String rawToken, String newPassword) {
        Usuario user = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String tokenHash = sha256Hex(rawToken);
        PasswordResetToken token = tokenRepo.findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new RuntimeException("Token inválido o ya utilizado"));

        if (!token.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Token no corresponde al usuario");

        if (token.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("El token ha expirado");

        user.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepo.save(user);

        token.setUsed(true);
        tokenRepo.save(token);
        log.info("Contraseña restablecida para el usuario {}", email);
    }

    private String generateToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generando hash SHA-256", e);
        }
    }

    private void sendResetEmail(String to, String link) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Recuperá tu contraseña - Tuinmo");
        msg.setText("""
                Hola,
                
                Recibimos una solicitud para restablecer tu contraseña en Tuinmo.
                Si fuiste vos, hacé clic en el siguiente enlace para crear una nueva contraseña:
                
                %s
                
                Este enlace expirará en %d minutos.
                Si no solicitaste esto, ignorá este mensaje.
                
                Saludos,
                El equipo de Tuinmo
                """.formatted(link, tokenExpiryMinutes));
        mailSender.send(msg);
    }
}
