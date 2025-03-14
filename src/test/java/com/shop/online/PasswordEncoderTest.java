package com.shop.online;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    
    @Test
    public void generateEncodedPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String[] passwords = {"123456", "admin123", "test123"};
        
        for (String password : passwords) {
            String encodedPassword = encoder.encode(password);
            System.out.println("Original password: " + password);
            System.out.println("Encoded password: " + encodedPassword);
            System.out.println();
        }
    }
} 