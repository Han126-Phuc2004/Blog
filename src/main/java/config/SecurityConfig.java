package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Cấu hình authorization
                .authorizeHttpRequests(auth -> auth
                        // Public access - Cho phép tất cả mọi người truy cập
                        .requestMatchers("/", "/index", "/home").permitAll()
                        .requestMatchers("/posts/**").permitAll()  // Chi tiết bài viết
                        .requestMatchers("/blog", "/blog/**").permitAll()  // Trang blog
                        .requestMatchers("/about", "/contact").permitAll()  // Các trang khác
                        .requestMatchers("/login", "/error").permitAll()  // Login và error pages
                        
                        // Static resources - CSS, JS, Images
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        
                        // API public endpoints (nếu cần)
                        .requestMatchers("/api/posts/**").permitAll()  // API xem posts
                        .requestMatchers("/api/auth/login", "/api/auth/check", 
                                       "/api/auth/public").permitAll()  // Public auth endpoints
                        .requestMatchers("/api/auth/logout", "/api/auth/me").authenticated()  // Cần login
                        
                        // Admin access - Chỉ ADMIN mới được truy cập
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/admin-only").hasRole("ADMIN")  // Admin-only API
                        
                        // API admin endpoints
                        .requestMatchers("/api/users/**", "/api/roles/**").hasRole("ADMIN")
                        
                        // Tất cả các request khác cần authentication
                        .anyRequest().authenticated()
                )
                
                // Cấu hình form login
                .formLogin(form -> form
                        .loginPage("/login")  // URL trang login
                        .loginProcessingUrl("/perform_login")  // URL xử lý login (Spring Security tự xử lý)
                        .defaultSuccessUrl("/admin/dashboard", true)  // Redirect sau khi login thành công
                        .failureUrl("/login?error=true")  // Redirect khi login thất bại
                        .permitAll()
                )
                
                // Cấu hình logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")  // Redirect về trang chủ sau khi logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                
                // CSRF protection (enable cho production)
                // Tạm disable để dễ test API với Postman
                .csrf(csrf -> csrf.disable())
                
                // Session management
                .sessionManagement(session -> session
                        .maximumSessions(1)  // Chỉ cho phép 1 session mỗi user
                        .maxSessionsPreventsLogin(false)  // Cho phép login mới và kick session cũ
                );
        
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

