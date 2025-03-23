package com.shop.online;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderTest {
    
    @Test
    public void generateEncodedPassword() {
        PasswordEncoder passwordEncoder;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String[] passwords = {"1212212", "232323", "123456"};
        
        for (String password : passwords) {
            String encodedPassword = encoder.encode(password);
            System.out.println("Original password: " + password);
            System.out.println("Encoded password: " + encodedPassword);
            System.out.println("Matches: " + encoder.matches(password, encodedPassword));
            System.out.println();
        }
    }
    
    @Test
    public void testAdminPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 数据库中存储的管理员密码密文
        String storedHash = "NON8vPaFVnQXgLmuZuMjju4NbCajuAeJWDzzJgrtcFITWilCX830a";
        
        // 测试常见密码
        String[] commonPasswords = {
            "123456", "admin123", "admin", "password", "123456789", 
            "admin@123", "Admin123", "administrator", "root", "123123"
        };
        
        System.out.println("测试管理员密码匹配:");
        for (String password : commonPasswords) {
            boolean matches = encoder.matches(password, storedHash);
            System.out.println("密码 '" + password + "' 匹配结果: " + matches);
        }
        
        // 加密一个新密码供管理员使用
        String newAdminPassword = "admin123";
        String encodedNewPassword = encoder.encode(newAdminPassword);
        System.out.println("\n如需重置管理员密码，可使用以下密文:");
        System.out.println("明文密码: " + newAdminPassword);
        System.out.println("密文密码: " + encodedNewPassword);
    }
    
    @Test
    public void checkSpecificPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 输入您需要验证的明文密码和密文
        String plainPassword = "YOUR_PLAIN_PASSWORD";
        String encodedPassword = "STORED_ENCODED_PASSWORD";
        
        boolean matches = encoder.matches(plainPassword, encodedPassword);
        System.out.println("密码匹配结果: " + matches);
        
        // 如果不匹配，创建一个新的加密密码
        if (!matches) {
            String newEncodedPassword = encoder.encode(plainPassword);
            System.out.println("为密码 '" + plainPassword + "' 生成的新密文:");
            System.out.println(newEncodedPassword);
        }
    }
    
    /**
     * 主方法，可以直接运行此类
     */
    public static void main(String[] args) {
        System.out.println("===== 密码编码测试工具 =====");
        System.out.println("这个工具可以帮助您测试BCrypt密码编码和验证\n");
        
        PasswordEncoderTest test = new PasswordEncoderTest();
        
        System.out.println("1. 生成常用密码的密文:");
        test.generateEncodedPassword();
        
        System.out.println("\n2. 测试管理员密码匹配:");
        test.testAdminPassword();
        
        System.out.println("\n===== 自定义密码检查 =====");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 数据库中的管理员密码密文
        String storedAdminHash = "NON8vPaFVnQXgLmuZuMjju4NbCajuAeJWDzzJgrtcFITWilCX830a";
        
        // 您可以在这里修改要测试的明文密码
        String[] testPasswords = {"admin", "123456", "password", "admin123"};
        
        for (String password : testPasswords) {
            boolean matches = encoder.matches(password, storedAdminHash);
            System.out.println("测试密码 '" + password + "' 与数据库密文匹配结果: " + matches);
        }
        
        // 生成新的管理员密码示例
        String newPassword = "admin";
        String encodedPassword = encoder.encode(newPassword);
        System.out.println("\n如需设置新管理员密码:");
        System.out.println("明文: " + newPassword);
        System.out.println("密文: " + encodedPassword);
    }
} 