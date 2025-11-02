
package controller;

import entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import service.RoleService;
import service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Integer id) {
        return userService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            // Check duplicate username
            if (userService.findByUsername(user.getUsername()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse("Username đã tồn tại"));
            }

            // Encode password
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            // Set role
            if (user.getRole() != null && user.getRole().getRoleId() != null) {
                var role = roleService.findById(user.getRole().getRoleId());
                user.setRole(role);
            }

            User savedUser = userService.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi tạo user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Integer id, @RequestBody User user) {
        user.setUserId(id);
        return userService.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteById(id);
    }

    // Error Response DTO
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
}