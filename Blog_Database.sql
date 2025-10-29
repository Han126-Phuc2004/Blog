-- ===============================
-- DATABASE: blog_platform
-- ===============================

DROP DATABASE IF EXISTS blog_platform;
CREATE DATABASE blog_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog_platform;

-- ===============================
-- 1. TABLE: roles
-- ===============================
CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

-- Sample Data
INSERT INTO roles (role_name)
VALUES ('ADMIN'), ('VISITOR');

-- ===============================
-- 2. TABLE: users
-- ===============================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    full_name NVARCHAR(100),
    role_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- Sample Data
INSERT INTO users (username, password, email, full_name, role_id)
VALUES
('admin', '123456', 'admin@blog.com', 'Administrator', 1),
('guest', 'guest123', 'guest@blog.com', 'Guest User', 2);

-- ===============================
-- 3. TABLE: posts
-- ===============================
CREATE TABLE posts (
    post_id INT AUTO_INCREMENT PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    author_id INT NOT NULL,
    FOREIGN KEY (author_id) REFERENCES users(user_id)
);

-- Sample Data
INSERT INTO posts (title, content, author_id)
VALUES
('Welcome to My Blog', 'This is the first post!', 1),
('Learning Spring Boot', 'Spring Boot makes development easy!', 1);

-- ===============================
-- 4. TABLE: comments
-- ===============================
CREATE TABLE comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Sample Data
INSERT INTO comments (content, post_id, user_id)
VALUES
('Great post!', 1, 2),
('Very helpful, thanks!', 2, 2);

-- ===============================
-- 5. (OPTIONAL) TABLE: tags
-- ===============================
CREATE TABLE tags (
    tag_id INT AUTO_INCREMENT PRIMARY KEY,
    tag_name VARCHAR(100) NOT NULL UNIQUE
);

-- Sample Data
INSERT INTO tags (tag_name) VALUES
('Spring Boot'),
('Java'),
('Web Development');

-- ===============================
-- 6. (OPTIONAL) TABLE: post_tags (Many-to-Many)
-- ===============================
CREATE TABLE post_tags (
    post_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
);

-- Sample Data
INSERT INTO post_tags (post_id, tag_id)
VALUES
(1, 2),
(2, 1),
(2, 3);

-- ===============================
-- ✅ VERIFY DATA
-- ===============================
SELECT * FROM roles;
SELECT * FROM users;
SELECT * FROM posts;
SELECT * FROM comments;
SELECT * FROM tags;
SELECT * FROM post_tags;

-- ===============================
-- END OF SCRIPT
-- ===============================
