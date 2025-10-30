package controller;

import entity.Post;
import entity.User;
import org.springframework.web.bind.annotation.*;
import service.PostService;
import service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    // GET all posts
    @GetMapping
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    // GET by ID
    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Integer id) {
        return postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    // GET by author
    @GetMapping("/author/{userId}")
    public List<Post> getPostsByAuthor(@PathVariable Integer userId) {
        return postService.findByAuthor(userId);
    }

    // CREATE
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        // Lấy User từ DB
        User author = userService.findById(post.getAuthor().getUserId());
        if (author == null) {
            throw new RuntimeException("Author not found");
        }
        post.setAuthor(author);
        return postService.save(post);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Integer id, @RequestBody Post updatedPost) {
        Post existing = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        existing.setTitle(updatedPost.getTitle());
        existing.setContent(updatedPost.getContent());
        return postService.save(existing);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable Integer id) {
        postService.deleteById(id);
        return "Deleted post with id " + id;
    }
}
