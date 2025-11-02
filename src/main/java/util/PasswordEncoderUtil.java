package util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class để generate BCrypt password
 * Sử dụng để tạo password mã hóa cho việc insert user vào database
 */
public class PasswordEncoderUtil {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("================================================================================");
        System.out.println("                       BCrypt Password Generator");
        System.out.println("================================================================================");
        System.out.println();
        
        // Danh sách các password cần mã hóa
        String[][] userPasswords = {
            {"admin", "123456", "admin@blog.com", "Administrator", "1"},
            {"admin", "admin123", "admin@blog.com", "Administrator", "1"},
            {"guest", "guest123", "guest@blog.com", "Guest User", "2"}
        };
        
        for (String[] userData : userPasswords) {
            String username = userData[0];
            String rawPassword = userData[1];
            String email = userData[2];
            String fullName = userData[3];
            String roleId = userData[4];
            
            String encodedPassword = encoder.encode(rawPassword);
            
            System.out.println("Username:         " + username);
            System.out.println("Raw Password:     " + rawPassword);
            System.out.println("Encoded Password: " + encodedPassword);
            System.out.println();
            System.out.println("SQL UPDATE:");
            System.out.println("UPDATE users SET password = '" + encodedPassword + "' WHERE username = '" + username + "';");
            System.out.println();
            System.out.println("SQL INSERT:");
            System.out.println("INSERT INTO users (username, password, email, full_name, role_id)");
            System.out.println("VALUES ('" + username + "', '" + encodedPassword + "', '" + email + "', '" + fullName + "', " + roleId + ");");
            System.out.println();
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println();
        }
        
        System.out.println("================================================================================");
        System.out.println("Copy SQL UPDATE statement va chay trong MySQL Workbench!");
        System.out.println("================================================================================");
    }
}

