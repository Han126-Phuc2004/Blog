package config;

import entity.Role;
import entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.RoleRepository;
import repository.UserRepository;

/**
 * Tạo dữ liệu mặc định cho database
 */
@Configuration
public class DatabaseInitializer {

    @Bean
    public CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        
        return args -> {
            System.out.println("\n========== DATABASE INITIALIZATION ==========");
            
            // Tạo roles nếu chưa có
            createRoleIfNotExists(roleRepository, "ADMIN");
            createRoleIfNotExists(roleRepository, "USER");
            
            // Tạo admin user nếu chưa có
            createAdminIfNotExists(userRepository, roleRepository, passwordEncoder);
            
            System.out.println("\n========== INITIALIZATION COMPLETE ==========");
            System.out.println("🌐 Web URL: http://localhost:8080/");
            System.out.println("🔐 Admin Login:");
            System.out.println("   Username: admin");
            System.out.println("   Password: admin123");
            System.out.println("\n📌 Admin Dashboard: http://localhost:8080/admin/dashboard");
            System.out.println("\n🧪 Test Authentication API:");
            System.out.println("   POST http://localhost:8080/api/auth/login");
            System.out.println("   GET  http://localhost:8080/api/auth/me");
            System.out.println("   GET  http://localhost:8080/api/auth/check");
            System.out.println("==============================================\n");
        };
    }

    private void createRoleIfNotExists(RoleRepository roleRepository, String roleName) {
        Role role = roleRepository.findByRoleName(roleName);
        if (role == null) {
            role = Role.builder()
                    .roleName(roleName)
                    .build();
            roleRepository.save(role);
            System.out.println("✅ Created role: " + roleName);
        } else {
            System.out.println("ℹ️  Role already exists: " + roleName);
        }
    }

    private void createAdminIfNotExists(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        
        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByRoleName("ADMIN");
            
            if (adminRole == null) {
                System.err.println("❌ ADMIN role not found! Cannot create admin user.");
                return;
            }
            
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))  // Mã hóa password
                    .email("admin@blog.com")
                    .fullName("Administrator")
                    .role(adminRole)
                    .build();
            
            userRepository.save(admin);
            System.out.println("✅ Created admin user: admin / admin123");
        } else {
            System.out.println("ℹ️  Admin user already exists");
        }
    }
}

