package controller;

import entity.Post;
import entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import service.PostService;
import service.UserService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin Controller để quản lý Posts (CRUD)
 * Chỉ Admin mới có quyền truy cập
 */
@RestController
@RequestMapping("/api/admin/posts")
@CrossOrigin(origins = "*")
public class AdminPostController {

    private final PostService postService;
    private final UserService userService;

    public AdminPostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    /**
     * GET /api/admin/posts - Xem tất cả posts
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
     * GET /api/admin/posts/{id} - Xem chi tiết post
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
     * POST /api/admin/posts - Tạo post mới
     */
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequest request, Authentication authentication) {
        try {
            // Lấy username từ authentication
            String username = authentication.getName();
            User author = userService.findByUsername(username);
            
            if (author == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Không tìm thấy user"));
            }

            Post post = new Post();
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            post.setAuthor(author);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());

            Post savedPost = postService.save(post);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi tạo post: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/posts/{id} - Cập nhật post
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Integer id, @RequestBody PostRequest request) {
        try {
            Post post = postService.findById(id)
                    .orElse(null);
            
            if (post == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Không tìm thấy post với ID: " + id));
            }

            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            post.setUpdatedAt(LocalDateTime.now());

            Post updatedPost = postService.save(post);
            return ResponseEntity.ok(updatedPost);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi cập nhật post: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/posts/{id} - Xóa post
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Integer id) {
        try {
            Post post = postService.findById(id)
                    .orElse(null);
            
            if (post == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Không tìm thấy post với ID: " + id));
            }

            postService.deleteById(id);
            return ResponseEntity.ok(new SuccessResponse("Xóa post thành công"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi xóa post: " + e.getMessage()));
        }
    }

    // DTOs
    static class PostRequest {
        private String title;
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

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

    static class SuccessResponse {
        private String message;
        private long timestamp;

        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}

