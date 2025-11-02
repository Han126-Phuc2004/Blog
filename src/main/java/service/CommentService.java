package service;

import entity.Comment;
import org.springframework.stereotype.Service;
import repository.CommentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // ==================== Standard Methods ====================
    
    // Lưu comment
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    // Lấy comment theo ID - trả về Comment hoặc null
    public Comment findById(Integer commentId) {
        return commentRepository.findById(commentId).orElse(null);
    }

    // Lấy tất cả comments
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    // Xóa comment theo ID
    public void deleteById(Integer commentId) {
        commentRepository.deleteById(commentId);
    }

    // Lấy comments theo Post ID
    public List<Comment> findByPostId(Integer postId) {
        return commentRepository.findByPost_PostId(postId);
    }

    // ==================== Alias Methods (Backward Compatibility) ====================
    
    // Lấy tất cả comments của 1 post
    public List<Comment> getCommentsByPost(Integer postId) {
        return findByPostId(postId);
    }

    // Lưu comment mới
    public Comment saveComment(Comment comment) {
        return save(comment);
    }

    // Xóa comment (dành cho admin)
    public void deleteComment(Integer commentId) {
        deleteById(commentId);
    }

    // Lấy tất cả comments (dành cho admin)
    public List<Comment> getAllComments() {
        return findAll();
    }
}
