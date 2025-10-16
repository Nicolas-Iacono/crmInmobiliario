package com.backend.crmInmobiliario.config;

import com.backend.crmInmobiliario.config.filter.JwtTokenValidator;
import com.backend.crmInmobiliario.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())

                // 🔹 Bloque OAuth2 (maneja /oauth2/authorization/google y /login/oauth2/code/google)
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("https://tuinmo.net?google_link=ok", true)
                        .failureUrl("https://tuinmo.net?google_link=error")
                )
                // 🔹 Registra internamente los filtros de OAuth2 Client
                .oauth2Client(Customizer.withDefaults())

                // ⚠️ Permitir sesión temporal SOLO si se requiere (necesario para Google OAuth)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // 🔹 Rutas públicas y protegidas
                .authorizeHttpRequests(authorize -> authorize
                        // Webhooks
                        .requestMatchers("/api/webhooks/mercadopago", "/api/webhooks/n8n/stripe/past-due").permitAll()
                        .requestMatchers("/api/subscriptions/provider-event").permitAll()

                        // Auth pública
                        .requestMatchers(HttpMethod.GET, "/usuario/check-username").permitAll()
                        .requestMatchers("/api/user/**", "/auth/**", "/oauth2/**", "/google/**").permitAll()

                        // Google OAuth (callback y estado)
                        .requestMatchers("/login/oauth2/**", "/rest/oauth2-credential/**", "/oauth/google/**").permitAll()

                        // Admin
                        .requestMatchers(HttpMethod.GET, "/user/all").hasRole("ADMIN")

                        // Inquilino
                        .requestMatchers("/api/inquilino/register", "/api/inquilino/login").permitAll()
                        .requestMatchers("/api/inquilino/recibos").hasRole("INQUILINO_USER")
                        .requestMatchers("/api/inquilino/contrato/**").hasRole("INQUILINO_USER")

                        // Presupuestos
                        .requestMatchers(HttpMethod.GET, "/api/presupuestos/usuario/**").authenticated()

                        // Todo lo demás accesible (ajustá si querés restringir más)
                        .anyRequest().permitAll()
                )

                // 🔹 Validación JWT para tus endpoints propios
                .addFilterBefore(jwtTokenValidator, BasicAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // ⚠️ En producción cambiá por BCrypt
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "https://darkgreen-ferret-296866.hostingersite.com",
                "http://localhost:3000",
                "https://tuinmo.net",
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "x-user-id",
                "x-username"
        ));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
