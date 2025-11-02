package com.blog.blogplatform.repository;

import com.blog.blogplatform.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    @Query("SELECT r FROM Role r WHERE r.role_name = :roleName")
    Optional<Role> findByRoleName(@Param("roleName") String roleName);
}
