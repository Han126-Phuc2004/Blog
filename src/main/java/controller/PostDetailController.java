package controller;

import entity.Post;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import service.PostService;

@Controller
public class PostDetailController {

    private final PostService postService;

    public PostDetailController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts/{id}")
    public String viewPost(@PathVariable Integer id, Model model) {
        try {
            Post post = postService.findById(id).orElse(null);
            if (post == null) {
                return "redirect:/blog?error";
            }
            model.addAttribute("post", post);
            return "post_detail";
        } catch (Exception e) {
            return "redirect:/blog?error";
        }
    }
}

