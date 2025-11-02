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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Public Controller cho Comments
 * Visitor có thể xem comments và thêm comment mới
 */
@RestController
@RequestMapping("/api/public/comments")
@CrossOrigin(origins = "*")
public class PublicCommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final UserService userService;

    public PublicCommentController(CommentService commentService, 
                                  PostService postService,
                                  UserService userService) {
        this.commentService = commentService;
        this.postService = postService;
        this.userService = userService;
    }

    /**
     * GET /api/public/comments/post/{postId} - Xem tất cả comments của một post
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getCommentsByPostId(@PathVariable Integer postId) {
        try {
            // Kiểm tra post có tồn tại không
            Post post = postService.findById(postId).orElse(null);
            if (post == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Không tìm thấy post với ID: " + postId));
            }

            List<Comment> comments = commentService.findByPostId(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi lấy comments: " + e.getMessage()));
        }
    }

    /**
     * POST /api/public/comments - Thêm comment mới (visitor)
     */
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CommentRequest request) {
        try {
            // Validate request
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Nội dung comment không được để trống"));
            }

            if (request.getPostId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Post ID không được để trống"));
            }

            if (request.getAuthorId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Author ID không được để trống"));
            }

            // Kiểm tra post có tồn tại không
            Post post = postService.findById(request.getPostId()).orElse(null);
            if (post == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Không tìm thấy post với ID: " + request.getPostId()));
            }

            // Kiểm tra user có tồn tại không
            User author = userService.findById(request.getAuthorId());
            if (author == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Không tìm thấy user với ID: " + request.getAuthorId()));
            }

            // Tạo comment mới
            Comment comment = new Comment();
            comment.setContent(request.getContent());
            comment.setPost(post);
            comment.setAuthor(author);
            comment.setCreatedAt(LocalDateTime.now());

            Comment savedComment = commentService.save(comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi tạo comment: " + e.getMessage()));
        }
    }

    // DTOs
    static class CommentRequest {
        private String content;
        private Integer postId;
        private Integer authorId;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Integer getPostId() { return postId; }
        public void setPostId(Integer postId) { this.postId = postId; }
        public Integer getAuthorId() { return authorId; }
        public void setAuthorId(Integer authorId) { this.authorId = authorId; }
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
}

