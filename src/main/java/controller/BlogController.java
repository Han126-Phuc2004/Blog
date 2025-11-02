package controller;

import entity.Post;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import service.PostService;

import java.util.List;

@Controller
public class BlogController {

    private final PostService postService;

    public BlogController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/blog")
    public String blog(Model model) {
        try {
            List<Post> posts = postService.findAll();
            model.addAttribute("posts", posts);
        } catch (Exception e) {
            // If database connection fails, pass empty list
            model.addAttribute("posts", new java.util.ArrayList<>());
        }
        return "blog";
    }
}

