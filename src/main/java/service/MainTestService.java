package service;

import entity.Role;
import entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import repository.RoleRepository;
import repository.UserRepository;

@SpringBootApplication(scanBasePackages = {"entity", "repository", "service"})
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "entity")
public class MainTestService implements CommandLineRunner {

    private final RoleService roleService;
    private final UserService userService;

    public MainTestService(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MainTestService.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("===== TEST CRUD START =====");

        // --- ROLE CRUD ---
        Role role = new Role();
        role.setRoleName("EDITOR");
//        //tạo
//        roleService.save(role);
//        System.out.println("Created Role: " + role.getRoleId() + " - " + role.getRoleName());

        Role foundRole = roleService.findById(10);
        System.out.println("Found Role: " + foundRole.getRoleName());
// update
        foundRole.setRoleName("UPDATED_EDITOR");
        roleService.save(foundRole);
        System.out.println("Updated Role: " + foundRole.getRoleName());
// delete
        roleService.deleteById(foundRole.getRoleId());
        System.out.println("Deleted Role ID: " + foundRole.getRoleId());

        // --- USER CRUD ---
        // tạo
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("123456");
        user.setEmail("john@example.com");
        user.setFullName("John Doe");
        user.setRole(roleService.findById(1)); // Gán role ADMIN chẳng hạn
        userService.save(user);
        System.out.println("Created User: " + user.getUsername());
// update
        User foundUser = userService.findById(user.getUserId());
        System.out.println("Found User: " + foundUser.getFullName());

        foundUser.setFullName("John Updated");
        userService.save(foundUser);
        System.out.println("Updated User: " + foundUser.getFullName());
// delete
        userService.deleteById(foundUser.getUserId());
        System.out.println("Deleted User ID: " + foundUser.getUserId());

        System.out.println("===== TEST CRUD END =====");
    }
}
