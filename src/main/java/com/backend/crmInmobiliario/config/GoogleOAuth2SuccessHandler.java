package com.backend.crmInmobiliario.config;

import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.impl.UserService;
import com.backend.crmInmobiliario.utils.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${app.oauth2.google.success-url:https://tuinmo.net/auth/google/callback}")
    private String successUrl;

    @Value("${app.oauth2.google.error-url:https://tuinmo.net/login}")
    private String errorUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String googleSub = oauth2User.getAttribute("sub");

        if (email == null || email.isBlank()) {
            response.sendRedirect(buildErrorUrl("google_email_no_disponible"));
            return;
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElse(null);

        if (usuario == null) {
            response.sendRedirect(buildErrorUrl("usuario_no_registrado_con_ese_email"));
            return;
        }

        usuario.setGoogleEmail(email.trim().toLowerCase());
        usuario.setGoogleId(googleSub);
        usuarioRepository.save(usuario);

        UserDetails userDetails = userService.loadUserByUsername(usuario.getUsername());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        String accessToken = jwtUtil.createAccessToken(auth, usuario.getId());
        String refreshToken = jwtUtil.createRefreshToken(auth);

        String redirectUrl = UriComponentsBuilder.fromUriString(successUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("provider", "google")
                .build(true)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private String buildErrorUrl(String reason) {
        return UriComponentsBuilder.fromUriString(errorUrl)
                .queryParam("oauth", "error")
                .queryParam("reason", reason)
                .build(true)
                .toUriString();
    }
}
