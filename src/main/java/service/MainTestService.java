package service;

import entity.Post;
import entity.Role;
import entity.Tag;
import entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"entity", "repository", "service"})
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "entity")
public class MainTestService implements CommandLineRunner {

    private final RoleService roleService;
    private final UserService userService;
    private final PostService postService;
    private final TagService tagService;

    public MainTestService(RoleService roleService, UserService userService, PostService postService, TagService tagService) {
        this.roleService = roleService;
        this.userService = userService;
        this.postService = postService;
        this.tagService = tagService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MainTestService.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("===== TEST CRUD START =====");

        try {
            // --- ROLE CRUD (SAFE VERSION) ---
            System.out.println("===== ROLE CRUD START =====");

            Role role = roleService.findByName("EDITOR");
            if (role == null) {
                role = new Role();
                role.setRoleName("EDITOR");
                roleService.save(role);
                System.out.println(" Created new Role: " + role.getRoleId() + " - " + role.getRoleName());
            } else {
                System.out.println("ℹ Role already exists: " + role.getRoleId() + " - " + role.getRoleName());
            }

            Role foundRole = roleService.findById(role.getRoleId());
            System.out.println(" Found Role: " + foundRole.getRoleName());

            String newRoleName = "UPDATED_EDITOR";
            if (roleService.findByName(newRoleName) == null) {
                foundRole.setRoleName(newRoleName);
                roleService.save(foundRole);
                System.out.println(" Updated Role name to: " + foundRole.getRoleName());
            } else {
                System.out.println(" Skipped update: Role name '" + newRoleName + "' already exists.");
            }

            System.out.println(" Skip deleting role to avoid foreign key constraint!");
            System.out.println("===== ROLE CRUD END =====\n");


            // --- USER CRUD ---
            System.out.println("===== USER CRUD START =====");
            User foundUser = userService.findByUsername("admin");
            if (foundUser == null) {
                User newUser = new User();
                newUser.setUsername("admin");
                newUser.setPassword("123456");
                newUser.setEmail("admin@example.com");
                newUser.setFullName("Administrator");
                newUser.setRole(foundRole);
                userService.save(newUser);
                foundUser = newUser;
                System.out.println(" Created new User: " + foundUser.getUsername());
            } else {
                System.out.println("ℹ User already exists: " + foundUser.getUsername());
            }

            User checkUser = userService.findById(foundUser.getUserId());
            System.out.println(" Found User: " + checkUser.getUsername() + " | Role: " + checkUser.getRole().getRoleName());

            checkUser.setFullName("Admin Updated");
            userService.save(checkUser);
            System.out.println(" Updated User full name: " + checkUser.getFullName());

            System.out.println(" Skip deleting user to preserve relations\n");
            System.out.println("===== USER CRUD END =====\n");


            // --- POST CRUD ---
            System.out.println("===== POST CRUD START =====");
            Post post = new Post();
            post.setTitle("Welcome to Spring Boot");
            post.setContent("This is the first post created from MainTestService.");
            post.setAuthor(foundUser);
            postService.save(post);
            System.out.println(" Created Post: " + post.getPostId() + " - " + post.getTitle());

            Post foundPost = postService.findById(post.getPostId())
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            System.out.println(" Found Post: " + foundPost.getTitle() + " | Author: " + foundPost.getAuthor().getUsername());

            foundPost.setContent("Spring Boot makes development easy!");
            postService.save(foundPost);
            System.out.println(" Updated Post content: " + foundPost.getContent());
            System.out.println(" Skip deleting post to test tags\n");
            System.out.println("===== POST CRUD END =====\n");


            // --- TAG CRUD ---
            System.out.println("===== TAG CRUD START =====");

            String[] defaultTags = {"Spring Boot", "Java", "Web Development"};
            for (String tagName : defaultTags) {
                Tag existing = tagService.findByName(tagName);
                if (existing == null) {
                    Tag newTag = new Tag();
                    newTag.setTagName(tagName);
                    tagService.save(newTag);
                    System.out.println(" Created Tag: " + newTag.getTagId() + " - " + newTag.getTagName());
                } else {
                    System.out.println("ℹ Tag already exists: " + existing.getTagId() + " - " + existing.getTagName());
                }
            }

            System.out.println("\n All Tags in Database:");
            tagService.findAll().forEach(tag ->
                    System.out.println("  - " + tag.getTagId() + ": " + tag.getTagName())
            );

            Tag firstTag = tagService.findAll().stream().findFirst().orElse(null);
            if (firstTag != null) {
                String updatedName = firstTag.getTagName() + " Updated " + System.currentTimeMillis();
                firstTag.setTagName(updatedName);
                tagService.save(firstTag);
                System.out.println("\n Updated Tag: " + firstTag.getTagId() + " - " + firstTag.getTagName());
            }

            Tag deleteTag = tagService.findByName("Web Development");
            if (deleteTag != null) {
                tagService.deleteById(deleteTag.getTagId());
                System.out.println("\n🗑 Deleted Tag ID: " + deleteTag.getTagId());
            }

            System.out.println("===== TAG CRUD END =====");

        } catch (Exception e) {
            System.err.println(" Error during CRUD: " + e.getMessage());
        }

        System.out.println("\n===== TEST CRUD END =====");
    }
}
