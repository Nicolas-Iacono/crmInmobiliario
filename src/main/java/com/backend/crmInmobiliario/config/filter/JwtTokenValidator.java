package com.backend.crmInmobiliario.config.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.crmInmobiliario.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
public class JwtTokenValidator extends OncePerRequestFilter {

    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("🟣 Request entrante: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("🔸 Origin: " + request.getHeader("Origin"));
        System.out.println("🔸 Authorization: " + request.getHeader("Authorization"));

        String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7);

            try {
                DecodedJWT decodedJWT = jwtUtil.validateAccessToken(jwtToken);

                String email = jwtUtil.extractUsername(decodedJWT);
                String stringAuthorities = jwtUtil.getSpecifClaim(decodedJWT, "authorities").asString();
                Long userId = jwtUtil.getSpecifClaim(decodedJWT, "userId").asLong(); // 🔹 acá obtenemos el userId

                Collection<? extends GrantedAuthority> authorities =
                        AuthorityUtils.commaSeparatedStringToAuthorityList(
                                stringAuthorities != null ? stringAuthorities : ""
                        );

                // 🔹 Creamos la autenticación con los roles
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                // 🔹 Guardamos el userId como detalle adicional del token
                authentication.setDetails(java.util.Map.of("userId", userId));

                // 🔹 Cargamos la autenticación en el contexto
                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
                return;

            } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"token_expired\"}");
                return;

            } catch (JWTVerificationException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"invalid_token\"}");
                return;
            }
        }
        System.out.println("🟣 Request entrante: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("🔸 Origin: " + request.getHeader("Origin"));
        System.out.println("🔸 Authorization: " + request.getHeader("Authorization"));

        // si no hay token, igual dejamos pasar (para endpoints permitAll)
        filterChain.doFilter(request, response);
    }

    private String resolveBearer(HttpServletRequest req) {
        String h = req.getHeader(HttpHeaders.AUTHORIZATION);
        return (h != null && h.startsWith("Bearer ")) ? h.substring(7) : null;
    }

    private String resolveCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) if (name.equals(c.getName())) return c.getValue();
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Todo lo público que NO debe validar JWT
        return path.equals("/api/inquilino/login")
                || path.equals("/api/propietario/login")
                || path.equals("/api/usuario/login")
                || path.equals("/api/aliado/login")

                || path.equals("/api/inquilino/register")
                || path.equals("/api/propietario/register")
                || path.equals("/api/usuario/registrar-admin")
                || path.equals("/api/oficios/categorias")
                || path.equals("/api/oficios/proveedores/registro")

                || path.equals("/api/webhooks/mercadopago")
                || path.equals("/api/webhooks/n8n/stripe/past-due")
                || path.equals("/api/subscriptions/provider-event")

                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
