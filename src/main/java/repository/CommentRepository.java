package repository;

import entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    
    // Tìm tất cả comments của 1 post (để hiển thị dưới bài viết)
    List<Comment> findByPost_PostId(Integer postId);
    
    // Tìm tất cả comments của 1 user
    List<Comment> findByAuthor_UserId(Integer userId);
}

