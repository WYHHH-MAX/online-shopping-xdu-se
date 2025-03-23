package com.shop.online.util;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 文件工具类
 */
public class FileUtil {
    
    // 文件上传根目录（可以通过配置文件配置）
    private static final String UPLOAD_DIR = "uploads";
    
    // 头像上传目录
    private static final String AVATAR_DIR = "avatars";
    
    // 商家资质上传目录
    private static final String SELLER_DIR = "seller";
    
    // 商品图片上传目录
    private static final String PRODUCT_DIR = "products";
    
    // 允许的图片格式
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg", "image/png", "image/gif"
    };
    
    /**
     * 上传用户头像
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像相对路径
     * @throws IOException 文件上传异常
     */
    public static String uploadUserAvatar(MultipartFile file, Long userId) throws IOException {
        // 验证文件是否为图片
        validateImageFile(file);
        
        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        
        // 生成文件名：user_用户ID.扩展名
        // 不再使用时间戳，确保同一用户的头像文件名保持一致，实现覆盖上传
        String filename = String.format("user_%d.%s", userId, extension);
        
        // 创建上传目录
        Path uploadPath = getAvatarUploadPath();
        
        // 检查是否存在旧文件（不同扩展名的情况）
        File[] existingFiles = new File(uploadPath.toString())
            .listFiles((dir, name) -> name.startsWith("user_" + userId + "."));
        
        // 删除旧文件（如果有不同扩展名的旧文件）
        if (existingFiles != null) {
            for (File oldFile : existingFiles) {
                if (!oldFile.getName().equals(filename)) {
                    oldFile.delete();
                }
            }
        }
        
        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回相对路径
        return "/" + UPLOAD_DIR + "/" + AVATAR_DIR + "/" + filename;
    }
    
    /**
     * 上传商家资质图片
     *
     * @param file 图片文件
     * @param sellerId 商家ID
     * @param fileType 文件类型(logo, businessLicenseImage, idCardFront, idCardBack)
     * @return 图片相对路径
     * @throws IOException 文件上传异常
     */
    public static String uploadSellerQualification(MultipartFile file, Long sellerId, String fileType) throws IOException {
        // 验证文件是否为图片
        validateImageFile(file);
        
        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        
        // 生成文件名：seller_商家ID_文件类型_时间戳.扩展名
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String filename = String.format("seller_%d_%s_%s.%s", sellerId, fileType, timestamp, extension);
        
        // 创建上传目录
        Path uploadPath = getSellerUploadPath();
        
        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回相对路径
        return "/" + UPLOAD_DIR + "/" + SELLER_DIR + "/" + filename;
    }
    
    /**
     * 上传商品图片
     *
     * @param file 图片文件
     * @param sellerId 商家ID
     * @return 图片相对路径
     * @throws IOException 文件上传异常
     */
    public static String uploadProductImage(MultipartFile file, Long sellerId) throws IOException {
        // 验证文件是否为图片
        validateImageFile(file);
        
        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        
        // 生成文件名：product_商家ID_时间戳.扩展名
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String filename = String.format("product_%d_%s.%s", sellerId, timestamp, extension);
        
        // 创建上传目录
        Path uploadPath = getProductUploadPath();
        
        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回相对路径
        return "/" + UPLOAD_DIR + "/" + PRODUCT_DIR + "/" + filename;
    }
    
    /**
     * 获取头像上传路径
     */
    private static Path getAvatarUploadPath() throws IOException {
        Path uploadDir = Paths.get(UPLOAD_DIR);
        Path avatarDir = uploadDir.resolve(AVATAR_DIR);
        
        // 创建目录（如果不存在）
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        if (!Files.exists(avatarDir)) {
            Files.createDirectories(avatarDir);
        }
        
        return avatarDir;
    }
    
    /**
     * 获取商家资质上传路径
     */
    private static Path getSellerUploadPath() throws IOException {
        Path uploadDir = Paths.get(UPLOAD_DIR);
        Path sellerDir = uploadDir.resolve(SELLER_DIR);
        
        // 创建目录（如果不存在）
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        if (!Files.exists(sellerDir)) {
            Files.createDirectories(sellerDir);
        }
        
        return sellerDir;
    }
    
    /**
     * 获取商品图片上传路径
     */
    private static Path getProductUploadPath() throws IOException {
        Path uploadDir = Paths.get(UPLOAD_DIR);
        Path productDir = uploadDir.resolve(PRODUCT_DIR);
        
        // 创建目录（如果不存在）
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        if (!Files.exists(productDir)) {
            Files.createDirectories(productDir);
        }
        
        return productDir;
    }
    
    /**
     * 验证文件是否为图片
     */
    private static void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        boolean isValidType = false;
        
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            throw new IllegalArgumentException("不支持的文件类型，只允许上传JPEG/PNG/GIF图片");
        }
        
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) { // 5MB限制
            throw new IllegalArgumentException("文件大小不合法，最大支持5MB");
        }
    }
} 