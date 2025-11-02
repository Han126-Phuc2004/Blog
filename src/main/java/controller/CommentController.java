package controller;

import entity.Comment;
import entity.Post;
import entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import service.CommentService;
import service.PostService;
import service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
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

    // ========================================
    // PUBLIC ENDPOINTS - Không cần authentication
    // ========================================

    /**
     * GET /api/public/posts/{postId}/comments
     * Lấy tất cả comments của 1 post (public)
     */
    @GetMapping("/public/posts/{postId}/comments")
    public ResponseEntity<Map<String, Object>> getCommentsByPost(@PathVariable Integer postId) {
        try {
            List<Comment> comments = commentService.findByPostId(postId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("comments", comments);
            response.put("totalComments", comments.size());
            response.put("postId", postId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Không thể lấy comments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/public/comments/{id}
     * Lấy 1 comment theo ID
     */
    @GetMapping("/public/comments/{id}")
    public ResponseEntity<?> getCommentById(@PathVariable Integer id) {
        try {
            Comment comment = commentService.findById(id);
            if (comment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Comment không tồn tại với id: " + id));
            }
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi: " + e.getMessage()));
        }
    }

    /**
     * POST /api/public/posts/{postId}/comments
     * Tạo comment mới cho 1 post (yêu cầu đăng nhập)
     */
    @PostMapping("/public/posts/{postId}/comments")
    public ResponseEntity<?> createComment(@PathVariable Integer postId, @RequestBody CommentRequest request) {
        try {
            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Bạn cần đăng nhập để comment"));
            }

            String username = authentication.getName();
            User author = userService.findByUsername(username);
            if (author == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("User không tồn tại"));
            }

            // Validate content
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Nội dung comment không được để trống"));
            }

            // Get post
            Post post = postService.findById(postId).orElse(null);
            if (post == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Post không tồn tại với id: " + postId));
            }

            // Create comment
            Comment comment = Comment.builder()
                    .content(request.getContent())
                    .post(post)
                    .author(author)
                    .build();

            Comment savedComment = commentService.save(comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi tạo comment: " + e.getMessage()));
        }
    }

    // ========================================
    // ADMIN ENDPOINTS - Cần authentication & role ADMIN
    // ========================================

    /**
     * GET /api/admin/comments
     * Lấy tất cả comments (Admin only)
     */
    @GetMapping("/admin/comments")
    public ResponseEntity<List<Comment>> getAllComments() {
        List<Comment> comments = commentService.findAll();
        return ResponseEntity.ok(comments);
    }

    /**
     * DELETE /api/admin/comments/{id}
     * Xóa comment (Admin only - xóa inappropriate comments)
     */
    @DeleteMapping("/admin/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer id) {
        try {
            Comment comment = commentService.findById(id);
            if (comment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Comment không tồn tại với id: " + id));
            }
            
            commentService.deleteById(id);
            return ResponseEntity.ok(new SuccessResponse("Xóa comment thành công với id: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi xóa comment: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/posts/{postId}/comments
     * Xóa tất cả comments của 1 post (Admin only)
     */
    @DeleteMapping("/admin/posts/{postId}/comments")
    public ResponseEntity<?> deleteCommentsByPostId(@PathVariable Integer postId) {
        try {
            List<Comment> comments = commentService.findByPostId(postId);
            
            for (Comment comment : comments) {
                commentService.deleteById(comment.getCommentId());
            }
            
            return ResponseEntity.ok(new SuccessResponse("Đã xóa " + comments.size() + " comments của post id: " + postId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi xóa comments: " + e.getMessage()));
        }
    }

    // ========================================
    // DTO Classes
    // ========================================

    @lombok.Data
    static class CommentRequest {
        private String content;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class ErrorResponse {
        private String error;
        private long timestamp = System.currentTimeMillis();

        public ErrorResponse(String error) {
            this.error = error;
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class SuccessResponse {
        private String message;
        private long timestamp = System.currentTimeMillis();

        public SuccessResponse(String message) {
            this.message = message;
        }
    }
}

