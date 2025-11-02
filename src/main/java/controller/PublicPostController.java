package controller;

import entity.Post;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.PostService;

import java.util.List;

/**
 * Public Controller cho Visitor
 * Visitor có thể xem danh sách posts và đọc chi tiết posts
 */
@RestController
@RequestMapping("/api/public/posts")
@CrossOrigin(origins = "*")
public class PublicPostController {

    private final PostService postService;

    public PublicPostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * GET /api/public/posts - Xem tất cả posts (public)
     */
    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        try {
            List<Post> posts = postService.findAll();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi lấy danh sách posts: " + e.getMessage()));
        }
    }

    /**
     * GET /api/public/posts/{id} - Xem chi tiết post (public)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Integer id) {
        try {
            Post post = postService.findById(id)
                    .orElse(null);
            
            if (post == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Không tìm thấy post với ID: " + id));
            }
            
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi lấy post: " + e.getMessage()));
        }
    }

    /**
     * GET /api/public/posts/search - Tìm kiếm posts theo title
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchPosts(@RequestParam(required = false) String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.ok(postService.findAll());
            }
            
            List<Post> posts = postService.findAll().stream()
                    .filter(post -> post.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                   post.getContent().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
            
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi tìm kiếm: " + e.getMessage()));
        }
    }

    // DTOs
    static class ErrorResponse {
        private String error;
        private long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }

        public String getError() { return error; }
        public long getTimestamp() { return timestamp; }
    }
}

