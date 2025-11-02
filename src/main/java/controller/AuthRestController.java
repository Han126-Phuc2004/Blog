package controller;

import dto.AuthResponse;
import dto.LoginRequest;
import entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import repository.UserRepository;
import security.JwtUtil;
import security.CustomUserDetailsService;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller cho JWT Authentication
 * 
 * Chuyển đổi từ Session-based sang JWT-based authentication
 * 
 * Endpoints:
 * - POST /api/auth/login - Đăng nhập và nhận JWT token
 * - GET /api/auth/me - Lấy thông tin user hiện tại (cần JWT)
 * - POST /api/auth/validate - Validate JWT token
 * - GET /api/auth/check - Kiểm tra authentication status
 * - GET /api/auth/admin-only - Test admin endpoint
 * - GET /api/auth/public - Test public endpoint
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")  // Allow all origins (có thể config cụ thể cho production)
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public AuthRestController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * POST /api/auth/login
     * 
     * Login và nhận JWT token
     * 
     * Request body:
     * {
     *   "username": "admin",
     *   "password": "admin123"
     * }
     * 
     * Response (success):
     * {
     *   "success": true,
     *   "message": "Login successful",
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "user": {
     *     "username": "admin",
     *     "email": "admin@blog.com",
     *     "fullName": "Administrator",
     *     "role": "ADMIN"
     *   }
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Authenticate user với username & password
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // 2. Load user details từ database
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            
            // 3. Generate JWT token
            String jwtToken = jwtUtil.generateToken(userDetails);

            // 4. Get user info từ database
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found"));
            }

            // 5. Build response với token và user info
            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getRoleName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", jwtToken);
            response.put("user", userInfo);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "success", false,
                    "message", "Invalid username or password"
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Login failed: " + e.getMessage()
                ));
        }
    }

    /**
     * POST /api/auth/validate
     * 
     * Validate JWT token
     * 
     * Request header:
     * Authorization: Bearer <token>
     * 
     * Response:
     * {
     *   "valid": true,
     *   "username": "admin",
     *   "message": "Token is valid"
     * }
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("valid", false);
                response.put("message", "No token provided");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token, userDetails)) {
                response.put("valid", true);
                response.put("username", username);
                response.put("message", "Token is valid");
                return ResponseEntity.ok(response);
            } else {
                response.put("valid", false);
                response.put("message", "Token is invalid or expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Token validation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * GET /api/auth/me
     * 
     * Lấy thông tin user hiện tại (cần JWT token)
     * 
     * Request header:
     * Authorization: Bearer <token>
     * 
     * Response:
     * {
     *   "username": "admin",
     *   "email": "admin@blog.com",
     *   "fullName": "Administrator",
     *   "role": "ADMIN",
     *   "authenticated": true
     * }
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() 
                || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated", "message", "Please login first"));
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
            }

            // Return user info
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("fullName", user.getFullName());
            userInfo.put("role", user.getRole().getRoleName());
            userInfo.put("authenticated", true);

            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error getting user info: " + e.getMessage()));
        }
    }

    /**
     * GET /api/auth/check
     * 
     * Kiểm tra authentication status (public endpoint)
     * 
     * Response:
     * {
     *   "authenticated": true,
     *   "username": "admin",
     *   "role": "ROLE_ADMIN"
     * }
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated() 
            && !authentication.getPrincipal().equals("anonymousUser")) {
            
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("UNKNOWN");

            response.put("authenticated", true);
            response.put("username", username);
            response.put("role", role);
        } else {
            response.put("authenticated", false);
            response.put("message", "Not authenticated");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/admin-only
     * 
     * Endpoint test chỉ dành cho ADMIN (cần JWT token với role ADMIN)
     * 
     * Request header:
     * Authorization: Bearer <admin_token>
     */
    @GetMapping("/admin-only")
    public ResponseEntity<Map<String, String>> adminOnly() {
        return ResponseEntity.ok(Map.of(
            "message", "This endpoint is only accessible by ADMIN role",
            "status", "success",
            "user", SecurityContextHolder.getContext().getAuthentication().getName()
        ));
    }

    /**
     * GET /api/auth/public
     * 
     * Endpoint public để test (không cần JWT token)
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        return ResponseEntity.ok(Map.of(
            "message", "This is a public endpoint, no authentication required",
            "status", "success"
        ));
    }

    /**
     * POST /api/auth/logout
     * 
     * Logout (với JWT, chỉ cần client xóa token)
     * 
     * Note: Với JWT stateless, server không track sessions.
     * Client chỉ cần xóa token trong localStorage/cookie.
     * 
     * Optional: Có thể implement token blacklist nếu cần revoke token ngay lập tức.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of(
            "success", "true",
            "message", "Logged out successfully. Please delete your token on client side."
        ));
    }
}
