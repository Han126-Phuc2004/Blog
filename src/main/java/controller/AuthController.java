package controller;

import dto.LoginRequest;
import dto.LoginResponse;
import entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import security.CustomUserDetailsService;
import security.JwtUtil;
import service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                         CustomUserDetailsService userDetailsService,
                         JwtUtil jwtUtil,
                         UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    /**
     * Endpoint đăng nhập cho Admin
     * POST /api/auth/login
     * Body: { "username": "admin", "password": "password123" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Xác thực username và password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            
            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);

            // Lấy thông tin user từ database
            User user = userService.findByUsername(loginRequest.getUsername());
            
            // Lấy role name
            String roleName = user.getRole().getRoleName();
            
            // Kiểm tra xem user có phải là ADMIN không
            if (!roleName.equalsIgnoreCase("ADMIN") && !roleName.equalsIgnoreCase("ROLE_ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Bạn không có quyền truy cập. Chỉ Admin mới được phép đăng nhập."));
            }

            // Trả về response với token
            LoginResponse response = new LoginResponse(
                    token,
                    user.getUsername(),
                    roleName
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Username hoặc password không đúng"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Đã xảy ra lỗi: " + e.getMessage()));
        }
    }

    /**
     * Endpoint kiểm tra xem token có hợp lệ không
     * GET /api/auth/validate
     * Header: Authorization: Bearer <token>
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(token, userDetails)) {
                    return ResponseEntity.ok(new MessageResponse("Token hợp lệ"));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Token không hợp lệ"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Token không hợp lệ: " + e.getMessage()));
        }
    }

    // Inner classes cho response
    static class ErrorResponse {
        private String error;
        private long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }

        public String getError() {
            return error;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    static class MessageResponse {
        private String message;
        private long timestamp;

        public MessageResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
