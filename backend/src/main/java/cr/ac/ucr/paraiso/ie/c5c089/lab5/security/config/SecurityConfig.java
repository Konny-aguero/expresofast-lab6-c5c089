package cr.ac.ucr.paraiso.ie.c5c089.lab5.security.config;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                .requestMatchers("/", "/index.html", "/login.html", "/styles.css", "/app.js", "/api.js", "/login.js", "/favicon.ico", "/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/catalogos/**").hasAnyRole("ADMIN", "OPERADOR")
                
                .requestMatchers(HttpMethod.GET, "/api/envios/optimizados").hasAnyRole("ADMIN", "OPERADOR", "CONDUCTOR")
                .requestMatchers(HttpMethod.POST, "/api/envios").hasAnyRole("ADMIN", "OPERADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/envios/*/estado").hasAnyRole("ADMIN", "CONDUCTOR")
                .requestMatchers(HttpMethod.GET, "/api/envios/*/bitacora").hasAnyRole("ADMIN", "OPERADOR")
                .requestMatchers("/api/vehiculos/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request,response,ex) -> {
                    response.setStatus(401); response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":401,\"error\":\"Sesión ausente, inválida o vencida\"}");
                })
                .accessDeniedHandler((request,response,ex) -> {
                    response.setStatus(403); response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":403,\"error\":\"No tiene permisos para esta acción\"}");
                }))
            .sessionManagement(sess -> sess
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var cors = new org.springframework.web.cors.CorsConfiguration();
        cors.setAllowedOriginPatterns(java.util.List.of("http://localhost:*", "http://127.0.0.1:*"));
        cors.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
        cors.setMaxAge(3600L);
        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}
