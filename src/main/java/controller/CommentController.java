package controller;

import entity.Comment;
import entity.Post;
import entity.User;
import org.springframework.web.bind.annotation.*;
import service.CommentService;
import service.PostService;
import service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final UserService userService;

    public CommentController(CommentService commentService,
                             PostService postService,
                             UserService userService) {
        this.commentService = commentService;
        this.postService = postService;
        this.userService = userService;
    }

    @GetMapping
    public List<Comment> getAll() {
        return commentService.findAll();
    }

    @GetMapping("/{id}")
    public Comment getById(@PathVariable Integer id) {
        return commentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    @GetMapping("/post/{postId}")
    public List<Comment> getByPost(@PathVariable Integer postId) {
        return commentService.findByPostId(postId);
    }

    @PostMapping
    public Comment create(@RequestBody Comment comment) {
        // resolve post
        Integer postId = comment.getPost() != null ? comment.getPost().getPostId() : null;
        if (postId == null) throw new RuntimeException("postId is required");
        Post post = postService.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // resolve author
        Integer authorId = comment.getAuthor() != null ? comment.getAuthor().getUserId() : null;
        if (authorId == null) throw new RuntimeException("authorId is required");
        User author = userService.findById(authorId);
        if (author == null) throw new RuntimeException("Author not found");

        comment.setPost(post);
        comment.setAuthor(author);
        return commentService.save(comment);
    }

    @PutMapping("/{id}")
    public Comment update(@PathVariable Integer id, @RequestBody Comment updated) {
        Comment existing = commentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        existing.setContent(updated.getContent());
        return commentService.save(existing);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        commentService.deleteById(id);
        return "Deleted comment with id " + id;
    }
}
