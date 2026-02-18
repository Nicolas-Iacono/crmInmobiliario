package com.backend.crmInmobiliario.config;

import com.backend.crmInmobiliario.config.filter.JwtTokenValidator;
import com.backend.crmInmobiliario.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    private static final AuthenticationEntryPoint API_401 = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    public SecurityConfig(@Lazy JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }
    // =========================
    // 1) CADENA API (/api/**)
    // =========================
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")               // <- Solo aplica a /api/**
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(API_401)) // <- 401 en vez de redirect

                .authorizeHttpRequests(auth -> auth
                        // --- Webhooks públicos ---
                        .requestMatchers("/api/webhooks/mercadopago",
                                "/api/webhooks/n8n/stripe/past-due",
                                "/api/subscriptions/provider-event").permitAll()

                        // --- Auth pública ---
                        .requestMatchers(HttpMethod.GET, "/usuario/check-username").permitAll()
                        .requestMatchers("/api/user/**", "/auth/**", "/oauth2/**", "/google/**").permitAll()
                        .requestMatchers("/login/oauth2/**", "/rest/oauth2-credential/**", "/oauth/google/**").permitAll()

                        // --- Admin ---
                        .requestMatchers(HttpMethod.GET, "/user/all").hasRole("ADMIN")

                        // --- Logins / registros ---
                        .requestMatchers("/api/usuario/login").permitAll()
                        .requestMatchers("/api/aliado/login").permitAll()
                        .requestMatchers("/api/usuario/registrar-admin").permitAll()
                        .requestMatchers("/api/oficios/categorias", "/api/oficios/proveedores/registro").permitAll()
                        .requestMatchers("/api/inquilino/register", "/api/inquilino/login").permitAll()
                        .requestMatchers("/api/propietario/register", "/api/propietario/login").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuario/cobro/**").permitAll()

                        // --- Roles especiales ---
                        .requestMatchers("/api/inquilino/create").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/inquilino/recibos").hasRole("INQUILINO_USER")
                        .requestMatchers("/api/inquilino/contrato/**").hasRole("INQUILINO_USER")
                        .requestMatchers("/api/propiedad/por-propietario/con-imagenes").hasRole("PROPIETARIO_USER")
                        .requestMatchers("/api/propietario/contratos/por-propietario").hasRole("PROPIETARIO_USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/documentos/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")
                        // --- Presupuestos ---
                        .requestMatchers(HttpMethod.GET, "/api/presupuestos/usuario/**").authenticated()

                        // --- Endpoints personales (JWT) ---
                        .requestMatchers("/api/recibo/me",
                                "/api/contrato/me",
                                "/api/propiedad/me",
                                "/api/inquilino/me",
                                "/api/propietario/me").authenticated()

                        // --- Todo lo demás en /api/** requiere JWT ---
                        .anyRequest().authenticated()
                )

                // Validador JWT antes del UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtTokenValidator, UsernamePasswordAuthenticationFilter.class);

        // OJO: NO se configura oauth2Login en esta cadena
        return http.build();
    }

    // =========================
    // 2) CADENA WEB (resto)
    // =========================
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // Para la parte web / OAuth, permitimos sesión (requerida por Spring OAuth2 Client)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authorizeHttpRequests(auth -> auth
                        // Permitimos explícitamente login de Google y rutas web abiertas
                        .requestMatchers("/login/oauth2/**",
                                "/rest/oauth2-credential/**",
                                "/oauth/google/**",
                                "/oauth2/**",
                                "/auth/**",
                                "/api/user/**",
                                "/usuario/check-username").permitAll()

                        // También podés abrir tus recursos estáticos si aplica (ej: /, /index.html, /static/**)
                        .requestMatchers("/", "/index.html", "/static/**", "/assets/**").permitAll()

                        // Cualquier otra ruta web la dejamos abierta o la podés cerrar si querés
                        .anyRequest().permitAll()
                )

                // Flujo de OAuth2 (esto solo rige en esta cadena, NO en /api/**)
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("https://tuinmo.net?google_link=ok", true)
                        .failureUrl("https://tuinmo.net?google_link=error")
                )
                .oauth2Client(Customizer.withDefaults());

        return http.build();
    }

    // =========================
    // Beans comunes
    // =========================
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration conf) throws Exception {
        return conf.getAuthenticationManager();
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
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
                "https://darkgreen-ferret-296866.hostingersite.com",
                "http://localhost:3000",
                "https://tuinmo.net",
                "https://www.tuinmo.net",
                "http://localhost:8080"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-user-id", "x-username"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
