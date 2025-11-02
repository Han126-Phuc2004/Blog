-- ============================================================================
-- Script Setup Admin User cho Blog System
-- ============================================================================
-- Hướng dẫn sử dụng:
-- 1. Chạy PasswordEncoderUtil.java để generate BCrypt password
-- 2. Copy password đã mã hóa
-- 3. Thay thế vào câu INSERT bên dưới
-- 4. Chạy script này trong MySQL database
-- ============================================================================

-- Bước 1: Tạo các roles nếu chưa có
INSERT IGNORE INTO roles (role_name) VALUES ('ADMIN');
INSERT IGNORE INTO roles (role_name) VALUES ('USER');

-- Kiểm tra role_id của ADMIN (thường là 1)
SELECT role_id, role_name FROM roles WHERE role_name = 'ADMIN';

-- ============================================================================
-- Bước 2: Tạo tài khoản Admin
-- ============================================================================
-- Lưu ý: Password dưới đây đã được mã hóa bằng BCrypt cho password: "admin123"
-- Bạn có thể thay đổi bằng cách chạy PasswordEncoderUtil.java

-- Xóa admin cũ nếu đã tồn tại (optional - chỉ dùng cho development)
-- DELETE FROM users WHERE username = 'admin';

-- Insert admin mới
-- Thay đổi password đã hash bên dưới bằng output từ PasswordEncoderUtil.java
INSERT INTO users (username, password, email, full_name, role_id) 
VALUES (
    'admin',
    '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36T8XpEK.pnN4QnFq1g4Jga',  -- BCrypt hash của "admin123"
    'admin@blog.com',
    'Administrator',
    (SELECT role_id FROM roles WHERE role_name = 'ADMIN' LIMIT 1)
);

-- ============================================================================
-- Bước 3: Tạo thêm một số user thử nghiệm (optional)
-- ============================================================================

-- User thường với password "user123"
INSERT INTO users (username, password, email, full_name, role_id) 
VALUES (
    'testuser',
    '$2a$10$YourEncodedPasswordHere',  -- Thay bằng BCrypt hash của "user123"
    'user@blog.com',
    'Test User',
    (SELECT role_id FROM roles WHERE role_name = 'USER' LIMIT 1)
);

-- ============================================================================
-- Bước 4: Kiểm tra kết quả
-- ============================================================================

-- Xem tất cả users và roles của họ
SELECT 
    u.user_id,
    u.username,
    u.email,
    u.full_name,
    r.role_name
FROM users u
JOIN roles r ON u.role_id = r.role_id
ORDER BY u.user_id;

-- Đếm số lượng admin
SELECT COUNT(*) as total_admins 
FROM users u 
JOIN roles r ON u.role_id = r.role_id 
WHERE r.role_name = 'ADMIN';

-- ============================================================================
-- Hướng dẫn test login
-- ============================================================================
/*

1. Start Spring Boot application

2. Test login với cURL:

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

3. Nếu thành công, bạn sẽ nhận được response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ADMIN",
  "message": "Đăng nhập thành công"
}

4. Sử dụng token để truy cập admin routes:

curl -X GET http://localhost:8080/api/admin/posts \
  -H "Authorization: Bearer <your_token_here>"

*/

-- ============================================================================
-- Troubleshooting
-- ============================================================================
/*

Nếu gặp lỗi "Username hoặc password không đúng":
1. Kiểm tra password có được mã hóa đúng bằng BCrypt không
2. Chạy PasswordEncoderUtil.java để tạo password mới
3. Update password trong database:
   UPDATE users SET password = '<new_bcrypt_hash>' WHERE username = 'admin';

Nếu gặp lỗi "Không có quyền truy cập":
1. Kiểm tra role_name phải là 'ADMIN':
   SELECT * FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.username = 'admin';
2. Đảm bảo role_id đúng với role ADMIN

*/

