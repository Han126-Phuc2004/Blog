package com.blog.blogplatform.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hash for admin password: 123456
        String adminHash = encoder.encode("123456");
        System.out.println("Admin password hash: " + adminHash);
        
        // Generate hash for guest password: guest123
        String guestHash = encoder.encode("guest123");
        System.out.println("Guest password hash: " + guestHash);
    }
}