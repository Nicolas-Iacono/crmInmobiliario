package com.backend.crmInmobiliario.utils;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // corre antes que los demás filtros
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    private Bucket bucketLogin() {

        // 5 intentos por minuto
        Refill refill = Refill.intervally(5, Duration.ofMinutes(1));

        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, refill))
                .build();
    }

    private Bucket bucketRegister() {

        Refill refill = Refill.intervally(3, Duration.ofHours(1));

        Bandwidth limit = Bandwidth.classic(3, refill);

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getServletPath();
        String ip = extractClientIp(req);

        Bucket bucket = null;

        if (isLogin(path)) {
            bucket = ipBuckets.computeIfAbsent("LOGIN:" + ip, k -> bucketLogin());
        } else if (isRegister(path)) {
            bucket = ipBuckets.computeIfAbsent("REGISTER:" + ip, k -> bucketRegister());
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"rate_limited\"}");
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean isLogin(String path) {
        return path.equals("/api/inquilino/login")
                || path.equals("/api/propietario/login")
                || path.equals("/api/usuario/login");
    }

    private boolean isRegister(String path) {
        return path.equals("/api/inquilino/register")
                || path.equals("/api/propietario/register")
                || path.equals("/api/usuario/registrar-admin");
    }

    private String extractClientIp(HttpServletRequest req) {
        // Si tenés proxy/reverse proxy, esto es clave:
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}