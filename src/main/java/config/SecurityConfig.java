package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import security.JwtAuthenticationFilter;

/**
 * Spring Security Configuration với JWT Authentication
 * 
 * Thay đổi từ Session-based sang JWT-based:
 * - STATELESS session (không dùng HTTP session)
 * - JWT Authentication Filter
 * - Bỏ formLogin & logout config
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserDetailsService userDetailsService, 
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (không cần cho stateless API)
                .csrf(csrf -> csrf.disable())
                
                // ==================== AUTHORIZATION CONFIGURATION ====================
                .authorizeHttpRequests(auth -> auth
                        // ===== PUBLIC ACCESS - Không cần authentication =====
                        // Web pages
                        .requestMatchers("/", "/index", "/home").permitAll()
                        .requestMatchers("/blog", "/blog/**").permitAll()
                        .requestMatchers("/posts/**").permitAll()
                        .requestMatchers("/about", "/contact").permitAll()
                        .requestMatchers("/login", "/error").permitAll()
                        
                        // Static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        
                        // Public API endpoints
                        .requestMatchers("/api/auth/**").permitAll()  // Login, register, etc.
                        .requestMatchers("/api/public/**").permitAll()  // Public APIs
                        .requestMatchers("/api/posts/**").permitAll()  // Xem posts (public)
                        
                        // ===== ADMIN ACCESS - Cần authentication + role ADMIN =====
                        .requestMatchers("/admin/**").hasRole("ADMIN")  // Admin web pages
                        .requestMatchers("/api/users/**").hasRole("ADMIN")  // User management API
                        .requestMatchers("/api/roles/**").hasRole("ADMIN")  // Role management API
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")  // Admin APIs
                        
                        // ===== ALL OTHER REQUESTS - Cần authentication =====
                        .anyRequest().authenticated()
                )
                
                // ==================== SESSION MANAGEMENT ====================
                // STATELESS: Không sử dụng HTTP session
                // Mọi request đều phải có JWT token trong Authorization header
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // ==================== ADD JWT FILTER ====================
                // Thêm JwtAuthenticationFilter trước UsernamePasswordAuthenticationFilter
                // Filter này sẽ validate JWT token cho mỗi request
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * Password encoder - Sử dụng BCrypt để mã hóa password
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager - Để xử lý authentication programmatically trong REST API
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return new ProviderManager(authProvider);
    }
}

