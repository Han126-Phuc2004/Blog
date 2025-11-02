package controller;

import entity.Post;
import entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import service.PostService;
import service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PostService postService;
    private final UserService userService;

    public AdminController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            List<Post> posts = postService.findAll();
            model.addAttribute("posts", posts);
        } catch (Exception e) {
            model.addAttribute("posts", new java.util.ArrayList<>());
        }
        return "admin/dashboard";
    }

    @GetMapping("/posts/new")
    public String showCreateForm(Model model) {
        try {
            List<User> users = userService.findAll();
            model.addAttribute("post", new Post());
            model.addAttribute("users", users);
        } catch (Exception e) {
            model.addAttribute("post", new Post());
            model.addAttribute("users", new java.util.ArrayList<>());
        }
        return "admin/create_post";
    }

    @PostMapping("/posts")
    public String createPost(@ModelAttribute Post post, @RequestParam Integer authorId) {
        try {
            User author = userService.findById(authorId);
            post.setAuthor(author);
            postService.save(post);
            return "redirect:/admin/dashboard?success";
        } catch (Exception e) {
            return "redirect:/admin/posts/new?error";
        }
    }

    @GetMapping("/posts/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        try {
            Post post = postService.findById(id).orElse(null);
            if (post == null) {
                return "redirect:/admin/dashboard?error";
            }
            List<User> users = userService.findAll();
            model.addAttribute("post", post);
            model.addAttribute("users", users);
            return "admin/edit_post";
        } catch (Exception e) {
            return "redirect:/admin/dashboard?error";
        }
    }

    @PostMapping("/posts/{id}")
    public String updatePost(@PathVariable Integer id, @ModelAttribute Post updatedPost, @RequestParam Integer authorId) {
        try {
            Post existing = postService.findById(id).orElse(null);
            if (existing == null) {
                return "redirect:/admin/dashboard?error";
            }
            User author = userService.findById(authorId);
            existing.setTitle(updatedPost.getTitle());
            existing.setContent(updatedPost.getContent());
            existing.setAuthor(author);
            if (updatedPost.getThumbnailUrl() != null && !updatedPost.getThumbnailUrl().isEmpty()) {
                existing.setThumbnailUrl(updatedPost.getThumbnailUrl());
            }
            postService.save(existing);
            return "redirect:/admin/dashboard?success";
        } catch (Exception e) {
            return "redirect:/admin/dashboard?error";
        }
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Integer id) {
        try {
            postService.deleteById(id);
            return "redirect:/admin/dashboard?delete_success";
        } catch (Exception e) {
            return "redirect:/admin/dashboard?error";
        }
    }
}

