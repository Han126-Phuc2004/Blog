package controller;

import entity.Post;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import service.PostService;

import java.util.List;

@Controller
public class HomeController {

    private final PostService postService;

    public HomeController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({"/", "/index"})
    public String home(Model model) {
        try {
            List<Post> allPosts = postService.findAll();
            
            // Get featured posts (first 3)
            List<Post> featuredPosts = allPosts.isEmpty() ? allPosts : allPosts.subList(0, Math.min(3, allPosts.size()));
            
            // Get latest posts (all posts for now)
            model.addAttribute("featured", featuredPosts);
            model.addAttribute("posts", allPosts);
        } catch (Exception e) {
            // If database connection fails, pass empty lists
            model.addAttribute("featured", new java.util.ArrayList<>());
            model.addAttribute("posts", new java.util.ArrayList<>());
        }
        return "index";
    }
}
