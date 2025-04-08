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
        
        // 返回相对路径（新路径格式，对应于static资源目录）
        return "/images/avatars/" + filename;
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
        
        // 生成文件名
        String filename;
        if ("logo".equals(fileType)) {
            // 对于logo，使用固定文件名格式，不使用时间戳，便于覆盖
            filename = String.format("seller_%d_%s.%s", sellerId, fileType, extension);
        } else {
            // 其他类型的资质文件仍使用时间戳
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            filename = String.format("seller_%d_%s_%s.%s", sellerId, fileType, timestamp, extension);
        }
        
        // 创建上传目录 - 在static目录下
        Path uploadPath = getSellerStaticUploadPath();
        
        try {
            // 保存文件，使用Java 8的文件API而不是Spring的MultipartFile接口
            // 这样可以避免SpringMVC临时文件可能遇到的问题
            byte[] bytes = file.getBytes();
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, bytes);
            
            // 确保文件已正确保存
            if (!Files.exists(filePath)) {
                throw new IOException("文件保存失败，无法确认文件已写入: " + filePath);
            }
            
            // 返回相对路径，不包含/api前缀，避免前端重复添加
            String relativePath = "/images/seller/" + filename;
            System.out.println("商家资质图片上传成功，路径: " + relativePath);
            return relativePath;
        } catch (IOException e) {
            System.err.println("商家资质图片上传失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 获取商家资质上传路径 - 在static目录下以便直接访问
     */
    private static Path getSellerStaticUploadPath() throws IOException {
        // 使用项目根目录
        String projectRoot = System.getProperty("user.dir");
        System.out.println("项目根目录: " + projectRoot);
        
        // 构建具体的图片保存路径: 项目目录/src/main/resources/static/images/seller
        Path staticPath = Paths.get(projectRoot, "src", "main", "resources", "static", "images");
        Path sellerDir = staticPath.resolve(SELLER_DIR);
        
        // 输出路径信息以便调试
        System.out.println("商家资质图片保存路径: " + sellerDir.toAbsolutePath());
        
        // 创建目录（如果不存在）
        if (!Files.exists(staticPath)) {
            Files.createDirectories(staticPath);
        }
        
        if (!Files.exists(sellerDir)) {
            Files.createDirectories(sellerDir);
        }
        
        return sellerDir;
    }
    
    /**
     * 上传商品图片
     *
     * @param file 图片文件
     * @param sellerId 卖家ID
     * @param productId 商品ID，如果为null表示临时图片
     * @param imageIndex 图片序号，如果为null将使用1（默认值）
     * @return 图片相对路径
     * @throws IOException 文件上传异常
     */
    public static String uploadProductImage(MultipartFile file, Long sellerId, Long productId, Integer imageIndex) throws IOException {
        // 验证文件是否为图片
        validateImageFile(file);
        
        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        
        System.out.println("上传商品图片: 原始文件名=" + originalFilename + ", 扩展名=" + extension);
        
        // 生成文件名
        String filename;
        if (productId != null) {
            // 使用商品ID和序号命名格式：productId_imageIndex.extension
            // 如果未提供imageIndex，默认使用1
            int idx = (imageIndex != null) ? imageIndex : 1;
            filename = String.format("%d_%d.%s", productId, idx, extension);
            System.out.println("使用正式文件名: " + filename);
        } else {
            // 临时文件使用时间戳命名：temp_sellerId_timestamp.extension
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            filename = String.format("temp_%d_%s.%s", sellerId, timestamp, extension);
            System.out.println("使用临时文件名: " + filename);
        }
        
        // 保存目录路径
        String relativePath = "/images/products/";
        
        try {
            // 获取项目路径
            Path productsDir = getProductImagesDir();
            System.out.println("商品图片保存目录: " + productsDir.toAbsolutePath());
            
            // 保存文件
            Path filePath = productsDir.resolve(filename);
            System.out.println("保存图片到: " + filePath.toAbsolutePath());
            
            // 确保目录存在
            Files.createDirectories(productsDir);
            
            // 复制文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 验证文件是否已保存
            if (Files.exists(filePath)) {
                System.out.println("图片保存成功: " + filePath.toAbsolutePath());
            } else {
                throw new IOException("文件复制完成，但无法验证文件是否存在: " + filePath.toAbsolutePath());
            }
            
            // 返回相对路径
            String fullRelativePath = relativePath + filename;
            System.out.println("返回的相对路径: " + fullRelativePath);
            return fullRelativePath;
        } catch (IOException e) {
            System.err.println("上传商品图片失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 根据商品ID删除所有相关图片
     * 
     * @param productId 商品ID
     * @return 删除的文件数量
     */
    public static int deleteProductImages(Long productId) {
        if (productId == null) {
            System.out.println("删除商品图片: 商品ID为空");
            return 0;
        }
        
        // 获取项目根路径和图片目录
        String projectRoot = System.getProperty("user.dir");
        String staticDir = projectRoot + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static";
        String productsDir = staticDir + File.separator + "images" + File.separator + "products";
        
        System.out.println("删除商品 " + productId + " 的图片，路径: " + productsDir);
        
        File productImageDir = new File(productsDir);
        if (!productImageDir.exists() || !productImageDir.isDirectory()) {
            System.err.println("图片目录不存在: " + productsDir);
            return 0;
        }
        
        // 构建图片文件名前缀
        String productIdPrefix = productId + "_";
        int deletedCount = 0;
        
        // 遍历目录查找匹配的文件
        File[] files = productImageDir.listFiles();
        if (files != null) {
            System.out.println("找到 " + files.length + " 个文件，开始匹配前缀 " + productIdPrefix);
            
            for (File file : files) {
                if (file.isFile() && file.getName().startsWith(productIdPrefix)) {
                    System.out.println("尝试删除文件: " + file.getAbsolutePath());
                    try {
                        boolean deleted = file.delete();
                        if (deleted) {
                            deletedCount++;
                            System.out.println("已删除商品图片: " + file.getAbsolutePath());
                        } else {
                            System.err.println("无法删除商品图片: " + file.getAbsolutePath());
                            // 尝试使用Files.delete
                            try {
                                Files.delete(file.toPath());
                                deletedCount++;
                                System.out.println("使用Files.delete成功删除: " + file.getAbsolutePath());
                            } catch (IOException e) {
                                System.err.println("Files.delete也失败: " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("删除文件时发生异常: " + e.getMessage());
                    }
                }
            }
        }
        
        System.out.println("商品 " + productId + " 共删除了 " + deletedCount + " 个图片文件");
        return deletedCount;
    }
    
    /**
     * 获取商品图片完整目录路径
     * 
     * @return 商品图片目录的Path对象
     * @throws IOException 创建目录失败时抛出异常
     */
    public static Path getProductImagesDir() throws IOException {
        String projectRoot = System.getProperty("user.dir");
        System.out.println("项目根目录: " + projectRoot);
        
        // 确保使用绝对路径
        String staticDir = projectRoot + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static";
        Path imagesDir = Paths.get(staticDir, "images");
        Path productsDir = imagesDir.resolve("products");
        
        System.out.println("静态资源目录: " + staticDir);
        System.out.println("图片目录: " + imagesDir.toAbsolutePath());
        System.out.println("商品图片目录: " + productsDir.toAbsolutePath());
        
        // 确保所有目录都存在
        if (!Files.exists(Paths.get(staticDir))) {
            System.out.println("创建静态资源目录: " + staticDir);
            Files.createDirectories(Paths.get(staticDir));
        }
        
        if (!Files.exists(imagesDir)) {
            System.out.println("创建图片目录: " + imagesDir.toAbsolutePath());
            Files.createDirectories(imagesDir);
        }
        
        if (!Files.exists(productsDir)) {
            System.out.println("创建商品图片目录: " + productsDir.toAbsolutePath());
            Files.createDirectories(productsDir);
        }
        
        // 验证目录可写
        if (!Files.isWritable(productsDir)) {
            System.out.println("警告: 商品图片目录不可写: " + productsDir.toAbsolutePath());
            // 尝试设置权限
            try {
                productsDir.toFile().setWritable(true);
                System.out.println("已设置商品图片目录为可写: " + productsDir.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("无法设置目录权限: " + e.getMessage());
            }
        }
        
        return productsDir;
    }
    
    /**
     * 获取头像上传路径
     */
    private static Path getAvatarUploadPath() throws IOException {
        // 使用项目根目录
        String projectRoot = System.getProperty("user.dir");
        Path uploadDir = Paths.get(projectRoot, "src", "main", "resources", "static", "images");
        Path avatarDir = uploadDir.resolve(AVATAR_DIR);
        
        System.out.println("头像上传目录: " + avatarDir.toAbsolutePath());
        
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