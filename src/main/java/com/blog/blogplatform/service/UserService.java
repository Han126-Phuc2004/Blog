package com.blog.blogplatform.service;

import com.blog.blogplatform.entity.Role;
import com.blog.blogplatform.entity.User;
import com.blog.blogplatform.repository.RoleRepository;
import com.blog.blogplatform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ Dùng khi người dùng đăng ký
    public User registerUser(String username, String password, String email, String fullName) {
        try {
            System.out.println("=== REGISTER USER DEBUG ===");
            System.out.println("Username: " + username);
            System.out.println("Email: " + email);
            System.out.println("Full Name: " + fullName);
            
            // Try to find VISITOR role
            Role visitorRole = roleRepository.findByRoleName("VISITOR")
                    .orElseThrow(() -> new RuntimeException("Role VISITOR not found"));
            
            System.out.println("Found role: " + visitorRole.getRole_name());

            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email != null ? email : username + "@example.com");
            user.setFullName(fullName != null ? fullName : username);
            user.setRole(visitorRole);

            System.out.println("Saving user to database...");
            User savedUser = userRepository.save(user);
            System.out.println("User saved with ID: " + savedUser.getUserId());
            
            return savedUser;
        } catch (Exception e) {
            System.err.println("ERROR in registerUser: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
