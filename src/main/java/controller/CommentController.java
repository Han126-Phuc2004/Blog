package controller;

import entity.Comment;
import entity.Post;
import entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // ============================================
    // LẤY COMMENTS CỦA 1 POST
    // ============================================
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Integer postId) {
        List<Comment> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    // ============================================
    // TẠO COMMENT MỚI
    // ============================================
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody Comment comment) {
        try {
            // Validate post
            if (comment.getPost() == null || comment.getPost().getPostId() == null) {
                return ResponseEntity.badRequest().body("Post ID is required");
            }
            
            // Validate author
            if (comment.getAuthor() == null || comment.getAuthor().getUserId() == null) {
                return ResponseEntity.badRequest().body("Author ID is required");
            }

            // Lấy Post từ database
            Post post = postService.findById(comment.getPost().getPostId())
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            // Lấy User từ database
            User author = userService.findById(comment.getAuthor().getUserId());
            if (author == null) {
                return ResponseEntity.badRequest().body("Author not found");
            }

            // Set relationships
            comment.setPost(post);
            comment.setAuthor(author);

            // Lưu comment
            Comment savedComment = commentService.saveComment(comment);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating comment: " + e.getMessage());
        }
    }
}
