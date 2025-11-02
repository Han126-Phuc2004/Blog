package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API
            .authorizeHttpRequests(auth -> auth
                // ==================== PUBLIC ACCESS ====================
                // Authentication
                .requestMatchers("/api/auth/**").permitAll()
                
                // Public - Visitor có thể xem posts
                .requestMatchers("/", "/api/public/**").permitAll()
                .requestMatchers("/api/public/posts", "/api/public/posts/**").permitAll()
                .requestMatchers("/api/public/comments/**").permitAll()
                
                // ==================== ADMIN ACCESS ====================
                // Admin - CRUD posts
                .requestMatchers("/api/admin/posts", "/api/admin/posts/**").hasRole("ADMIN")
                
                // Admin - View và delete comments
                .requestMatchers("/api/admin/comments", "/api/admin/comments/**").hasRole("ADMIN")
                
                // Admin dashboard
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // ==================== OTHER ROUTES ====================
                // Tất cả các request khác cần xác thực
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


