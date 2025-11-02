package security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * Filter này sẽ intercept MỌI HTTP REQUEST và:
 * 1. Extract JWT token từ Authorization header
 * 2. Validate JWT token
 * 3. Load user details từ database
 * 4. Set authentication vào SecurityContext
 * 5. Cho phép request tiếp tục đến controller
 * 
 * OncePerRequestFilter: Đảm bảo filter chỉ chạy 1 lần cho mỗi request
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ========== 1. EXTRACT JWT TOKEN ==========
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // Kiểm tra xem header có chứa Bearer token không
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7); // Remove "Bearer " prefix
            
            try {
                // Extract username từ token
                username = jwtUtil.extractUsername(jwtToken);
            } catch (Exception e) {
                // Token invalid (malformed, expired, signature mismatch)
                logger.error("Error extracting username from JWT token: " + e.getMessage());
            }
        }

        // ========== 2. VALIDATE TOKEN & SET AUTHENTICATION ==========
        // Nếu có username và chưa có authentication trong SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            try {
                // Load user details từ database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Validate token
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    
                    // Tạo authentication object
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // credentials (password) - không cần vì đã authenticate bằng token
                                    userDetails.getAuthorities() // roles/permissions
                            );

                    // Set thêm request details
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Set authentication vào SecurityContext
                    // Từ giờ Spring Security sẽ biết user này đã authenticated
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    
                    logger.info("JWT Authentication successful for user: " + username);
                } else {
                    logger.warn("JWT token validation failed for user: " + username);
                }
                
            } catch (Exception e) {
                logger.error("Error during JWT authentication: " + e.getMessage());
            }
        }

        // ========== 3. CONTINUE FILTER CHAIN ==========
        // Cho phép request tiếp tục đến controller
        // Nếu authentication successful → Request sẽ được process bình thường
        // Nếu authentication failed → Spring Security sẽ reject request (401/403)
        filterChain.doFilter(request, response);
    }

    /**
     * Optional: Skip filter cho một số endpoints nhất định
     * Ví dụ: /api/auth/login không cần validate JWT
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Không filter các public endpoints
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/public/") ||
               path.equals("/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/static/") ||
               path.startsWith("/error");
    }
}

