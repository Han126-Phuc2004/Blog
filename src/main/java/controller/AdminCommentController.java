package controller;

import entity.Comment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.CommentService;

import java.util.List;

/**
 * Admin Controller để quản lý Comments
 * Admin có thể xem tất cả comments và xóa comments không phù hợp
 */
@RestController
@RequestMapping("/api/admin/comments")
@CrossOrigin(origins = "*")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * GET /api/admin/comments - Xem tất cả comments
     */
    @GetMapping
    public ResponseEntity<?> getAllComments() {
        try {
            List<Comment> comments = commentService.findAll();
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi lấy danh sách comments: " + e.getMessage()));
        }
    }

    /**
     * GET /api/admin/comments/{id} - Xem chi tiết comment
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCommentById(@PathVariable Integer id) {
        try {
            Comment comment = commentService.findById(id);
            
            if (comment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Không tìm thấy comment với ID: " + id));
            }
            
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi lấy comment: " + e.getMessage()));
        }
    }

    /**
     * GET /api/admin/comments/post/{postId} - Xem tất cả comments của một post
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getCommentsByPostId(@PathVariable Integer postId) {
        try {
            List<Comment> comments = commentService.findByPostId(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi lấy comments: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/comments/{id} - Xóa comment không phù hợp
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer id) {
        try {
            Comment comment = commentService.findById(id);
            
            if (comment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Không tìm thấy comment với ID: " + id));
            }

            commentService.deleteById(id);
            return ResponseEntity.ok(new SuccessResponse("Xóa comment thành công", id));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi xóa comment: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/comments/post/{postId} - Xóa tất cả comments của một post
     */
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<?> deleteCommentsByPostId(@PathVariable Integer postId) {
        try {
            List<Comment> comments = commentService.findByPostId(postId);
            
            for (Comment comment : comments) {
                commentService.deleteById(comment.getCommentId());
            }
            
            return ResponseEntity.ok(new SuccessResponse(
                "Đã xóa " + comments.size() + " comments của post ID: " + postId, 
                postId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi xóa comments: " + e.getMessage()));
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

    static class SuccessResponse {
        private String message;
        private Integer id;
        private long timestamp;

        public SuccessResponse(String message, Integer id) {
            this.message = message;
            this.id = id;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() { return message; }
        public Integer getId() { return id; }
        public long getTimestamp() { return timestamp; }
    }
}

