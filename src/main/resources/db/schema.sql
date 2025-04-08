CREATE DATABASE IF NOT EXISTS online_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE online_shop;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(200) COMMENT '头像',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '角色 0-买家 1-卖家',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'
) COMMENT '用户表';

-- 商品分类表
CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    level TINYINT NOT NULL COMMENT '层级 1-一级 2-二级 3-三级',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    icon VARCHAR(200) COMMENT '图标',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'
) COMMENT '商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL COMMENT '卖家ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    sales INT NOT NULL DEFAULT 0 COMMENT '销量',
    main_image VARCHAR(200) NOT NULL COMMENT '主图',
    is_featured TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐 0-否 1-是',
    featured_sort INT NOT NULL DEFAULT 0 COMMENT '推荐排序',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'
) COMMENT '商品表';

-- 商品图片表
CREATE TABLE IF NOT EXISTS product_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '商品ID',
    image_url VARCHAR(200) NOT NULL COMMENT '图片URL',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'
) COMMENT '商品图片表';

-- 购物车表
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    selected TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中 0-未选中 1-选中',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'
) COMMENT '购物车表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    seller_id BIGINT NOT NULL COMMENT '卖家ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'
    payment_method VARCHAR(10) DEFAULT NULL COMMENT '支付方式: 1-支付宝，2-微信支付，3-银行卡，4-货到付款';
) COMMENT '订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    product_image VARCHAR(200) NOT NULL COMMENT '商品图片',
    price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    quantity INT NOT NULL COMMENT '购买数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'
) COMMENT '订单明细表';

-- Product Review Table
CREATE TABLE IF NOT EXISTS product_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL COMMENT 'Order number',
    user_id BIGINT NOT NULL COMMENT 'User ID of the reviewer',
    product_id BIGINT NOT NULL COMMENT 'Product ID',
    rating TINYINT NOT NULL COMMENT 'Rating (1-5 stars)',
    content TEXT COMMENT 'Review content',
    images VARCHAR(500) COMMENT 'Review images (comma separated URLs)',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete flag'
) COMMENT 'Product review table';

-- Add indexes for fast query
CREATE INDEX idx_product_review_product_id ON product_review(product_id);
CREATE INDEX idx_product_review_user_id ON product_review(user_id);
CREATE INDEX idx_product_review_order_no ON product_review(order_no);

-- 商家表
CREATE TABLE IF NOT EXISTS seller (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    shop_name VARCHAR(50) NOT NULL COMMENT '店铺名称',
    shop_logo VARCHAR(200) COMMENT '店铺logo',
    shop_desc TEXT COMMENT '店铺描述',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-待审核 1-审核通过 2-审核拒绝',
    contact_name VARCHAR(50) COMMENT '联系人姓名',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    business_license VARCHAR(100) COMMENT '营业执照号',
    business_license_image VARCHAR(200) COMMENT '营业执照图片',
    id_card_front VARCHAR(200) COMMENT '身份证正面照片',
    id_card_back VARCHAR(200) COMMENT '身份证背面照片',
    reject_reason VARCHAR(255) COMMENT '审核拒绝理由',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'
) COMMENT '商家表';

INSERT INTO product (id, seller_id, category_id, name, description, price, stock, status, sales, main_image, is_featured, featured_sort, created_time, updated_time, deleted)
VALUES
    (200, 1, 80, 'Product 200', NULL, 0.00, 0, 1, 0, '/images/products/1_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (150, 1, 100, 'Product 150', NULL, 0.00, 0, 1, 0, '/images/products/2_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (100, 1, 60, 'Product 100', NULL, 0.00, 0, 1, 0, '/images/products/3_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (120, 1, 70, 'Product 120', NULL, 0.00, 0, 1, 0, '/images/products/4_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (30, 1, 15, 'Product 30', NULL, 0.00, 0, 1, 0, '/images/products/5_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (20, 1, 10, 'Product 20', NULL, 0.00, 0, 1, 0, '/images/products/6_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (100, 1, 45, 'Product 100', NULL, 0.00, 0, 1, 0, '/images/products/7_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (80, 1, 35, 'Product 80', NULL, 0.00, 0, 1, 0, '/images/products/8_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (500, 1, 200, 'Product 500', NULL, 0.00, 0, 1, 0, '/images/products/9_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (1000, 1, 300, 'Product 1000', NULL, 0.00, 0, 1, 0, '/images/products/10_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (100, 1, 50, 'Product 100', NULL, 0.00, 0, 1, 0, '/images/products/11_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (200, 1, 80, 'Product 200', NULL, 0.00, 0, 1, 0, '/images/products/12_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (50, 1, 20, 'Product 50', NULL, 0.00, 0, 1, 0, '/images/products/13_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (100, 1, 60, 'Product 100', NULL, 0.00, 0, 1, 0, '/images/products/14_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (150, 1, 70, 'Product 150', NULL, 0.00, 0, 1, 0, '/images/products/15_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0),
    (80, 1, 30, 'Product 80', NULL, 0.00, 0, 1, 0, '/images/products/16_1.jpg', 0, 0, '2025-03-11 13:36:28', '2025-03-13 13:56:06', 0);

INSERT INTO category (id, parent_id, name, level, sort, icon, created_time, updated_time, deleted)
VALUES
    (1, 0, '电子产品', 1, 1, 'mobile', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (2, 0, '服装', 1, 2, 'skin', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (3, 0, '家居', 1, 3, 'home', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (4, 0, '房产', 1, 4, 'bank', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (5, 0, '食品', 1, 5, 'coffee', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (6, 0, '娱乐', 1, 6, 'play-circle', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (7, 0, '书籍', 1, 7, 'book', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (8, 1, '手机', 2, 1, 'mobile', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (9, 1, '电脑', 2, 2, 'laptop', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (10, 1, '平板', 2, 3, 'tablet', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (11, 1, '智能手表', 2, 4, 'clock-circle', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (12, 2, '男装', 2, 1, 'user', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (13, 2, '女装', 2, 2, 'woman', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (14, 2, '童装', 2, 3, 'smile', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (15, 2, '运动装', 2, 4, 'trophy', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (16, 3, '家具', 2, 1, 'home', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (17, 3, '家电', 2, 2, 'tool', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (18, 3, '厨具', 2, 3, 'coffee', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (19, 3, '装饰', 2, 4, 'gift', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (20, 4, '新房', 2, 1, 'bank', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (21, 4, '二手房', 2, 2, 'shop', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (22, 4, '租房', 2, 3, 'home', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (23, 4, '商铺', 2, 4, 'shop', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (24, 5, '零食', 2, 1, 'coffee', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (25, 5, '饮料', 2, 2, 'coffee', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (26, 5, '生鲜', 2, 3, 'shopping', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (27, 5, '粮油', 2, 4, 'shopping', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (28, 6, '游戏', 2, 1, 'gamepad', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (29, 6, '电影', 2, 2, 'video-camera', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (30, 6, '音乐', 2, 3, 'sound', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (31, 6, '运动', 2, 4, 'trophy', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (32, 7, '文学', 2, 1, 'book', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (33, 7, '教育', 2, 2, 'read', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (34, 7, '科技', 2, 3, 'experiment', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0),
    (35, 7, '艺术', 2, 4, 'picture', '2025-03-10 22:40:53', '2025-03-10 22:40:53', 0);

INSERT INTO product_image (id, product_id, image_url, sort, created_time, updated_time, deleted)
VALUES
    (1, 1, '/images/products/1_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (2, 2, '/images/products/2_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (3, 3, '/images/products/3_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (4, 4, '/images/products/4_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (5, 5, '/images/products/5_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (6, 6, '/images/products/6_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (7, 7, '/images/products/7_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (8, 8, '/images/products/8_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (9, 9, '/images/products/9_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (10, 10, '/images/products/10_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (11, 11, '/images/products/11_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (12, 12, '/images/products/12_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (13, 13, '/images/products/13_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (14, 14, '/images/products/14_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (15, 15, '/images/products/15_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0),
    (16, 16, '/images/products/16_1.jpg', 0, '2025-03-12 13:54:03', '2025-03-12 13:54:03', 0);

INSERT INTO seller (id, user_id, shop_name, shop_logo, shop_desc, status, created_time, updated_time, deleted)
VALUES
    (1, 1, '电子数码专营店', 'https://example.com/logos/1.jpg', '专营各类电子产品，品质保证', 1, '2025-03-11 13:36:08', '2025-03-11 13:36:08', 0),
    (2, 2, '时尚服饰店', 'https://example.com/logos/2.jpg', '潮流服饰，引领时尚', 1, '2025-03-11 13:36:08', '2025-03-11 13:36:08', 0),
    (3, 3, '家居生活馆', 'https://example.com/logos/3.jpg', '打造温馨舒适的家', 1, '2025-03-11 13:36:08', '2025-03-11 13:36:08', 0),
    (4, 4, '美食天地', 'https://example.com/logos/4.jpg', '美食汇聚，品质生活', 1, '2025-03-11 13:36:08', '2025-03-11 13:36:08', 0),
    (5, 5, '文化书店', 'https://example.com/logos/5.jpg', '传播知识，分享智慧', 1, '2025-03-11 13:36:08', '2025-03-11 13:36:08', 0);

INSERT INTO user (id, username, password, nickname, phone, email, avatar, role, status, created_time, updated_time, deleted)
VALUES
    (12, 'buyer', '$2a$10$ZDk56IbosKbbz2nCthZyJuldA3I6ZXwwlXgORbu8V/HPIl8JCNPrq', '测试买家', '13800138002', 'buyer@example.com', NULL, 0, 1, '2025-03-09 18:01:40', '2025-03-09 18:04:48', 0),
    (13, 'buyer001', '$2a$10$oNbGrKhkOseYXKa3wG1ZJe6NU5MCCLfISr.8NnFg5ogMQCLA1B3lW', '测试买家1', '13800138001', 'buyer001@test.com', NULL, 0, 1, '2025-03-09 18:17:23', '2025-03-09 18:17:23', 0);