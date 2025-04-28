# 在线商城系统

## 项目介绍

这是一个基于Spring Boot和Vue.js的完整电子商城系统，提供了用户注册、商品浏览、购物车管理、订单处理和商家管理等功能。系统采用前后端分离架构，后端使用Java Spring Boot提供RESTful API，前端使用Vue.js构建响应式用户界面。

## 功能特性

### 用户功能
- 用户注册和登录
- 用户个人资料管理
- 头像上传和修改
- 商品浏览和搜索
- 购物车管理
- 订单创建和管理
- 订单支付和确认收货

### 商家功能
- 商家入驻申请
- 商品管理（添加、编辑、删除）
- 商品库存管理
- 订单处理和发货
- 销售数据统计

### 管理员功能
- 用户管理
-.商家审核
- 商品类目管理
- 订单监控

## 技术栈

### 后端
- Java 8
- Spring Boot 2.x
- Spring Security
- MyBatis-Plus
- MySQL
- JWT认证
- Lombok

### 前端
- Vue.js 3
- Ant Design Vue
- Axios
- Vite
- TypeScript

## 项目结构

```
spm2/
├── frontend/                 # 前端代码
│   ├── public/               # 静态资源
│   │   ├── api/              # API请求
│   │   ├── assets/           # 静态资源
│   │   ├── components/       # 通用组件
│   │   ├── layout/           # 布局组件
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # 状态管理
│   │   ├── types/            # TypeScript类型
│   │   ├── utils/            # 工具函数
│   │   └── views/            # 页面组件
│   ├── package.json          # 依赖配置
│   └── vite.config.ts        # Vite配置
│
├── src/                      # 后端代码
│   ├── main/                 # 主应用
│   │   ├── java/             # Java源代码
│   │   │   └── com/shop/online/
│   │   │       ├── config/           # 配置类
│   │   │       ├── controller/       # 控制器
│   │   │       ├── entity/           # 实体类
│   │   │       ├── exception/        # 异常处理
│   │   │       ├── mapper/           # MyBatis映射器
│   │   │       ├── security/         # 安全配置
│   │   │       ├── service/          # 服务层
│   │   │       ├── util/             # 工具类
│   │   │       └── vo/               # 值对象
│   │   └── resources/        # 资源文件
│   │       ├── mapper/       # MyBatis映射XML
│   │       ├── static/       # 静态资源
│   │       └── application.yml # 应用配置
│   └── test/                 # 测试代码
│
├── uploads/                 # 文件上传目录
│   ├── avatars/             # 用户头像
│   ├── products/            # 商品图片
│   └── seller/              # 商家资质
│
├── pom.xml                  # Maven配置
└── README.md                # 项目说明
```

## 安装和运行

### 前置条件
- JDK 8+
- Maven 3.6+
- Node.js 14+
- MySQL 5.7+

### 后端设置
1. 克隆项目
   ```bash
   git clone https://github.com/yourusername/shop-online.git
   cd shop-online
   ```

2. 配置数据库
   - 创建MySQL数据库
   - 修改`src/main/resources/application.yml`中的数据库连接信息

3. 构建并运行
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### 前端设置
1. 进入前端目录
   ```bash
   cd frontend
   ```

2. 安装依赖
   ```bash
   npm install
   ```

3. 开发环境运行
   ```bash
   npm run dev
   ```

4. 构建生产环境
   ```bash
   npm run build
   ```

## API参考

详细的API文档请参考[API参考文档](#api-reference)部分。

## 项目状态图和活动图

### 状态图
项目包含以下主要状态:
- 用户状态 (未登录/已登录)
- 商品状态 (创建中/已上架/未上架/已删除)
- 购物车状态 (空购物车/有商品/结算中)
- 订单状态 (待付款/待发货/待收货/已完成/已取消)
- 商家申请状态 (待提交/待审核/审核通过/审核拒绝)

### 活动图
系统主要活动包括:
- 用户认证流程
- 商品浏览和搜索
- 购物车管理
- 订单处理
- 商家管理

## 许可证

本项目使用 [MIT 许可证](LICENSE)。

## Payment Simulation Feature

The application now supports simulated WeChat and Alipay payments. Here's how it works:

### For Merchants:
1. During merchant registration or later in the merchant settings, merchants can upload QR code images for WeChat Pay and Alipay.
2. These QR codes are stored in the `src/main/resources/static/images/pay` directory with filenames based on the merchant ID and payment type.
3. If a merchant doesn't upload their own QR codes, default QR codes are automatically generated.

### For Customers:
1. During checkout, customers can select their preferred payment method (Alipay, WeChat Pay, etc.).
2. For WeChat Pay and Alipay, customers are redirected to a payment page showing the merchant's QR code.
3. On this page, customers can simulate a successful payment by clicking the "Simulate Payment" button.
4. Upon successful payment, the order status is updated, and the customer is redirected to a payment success page.

### Implementation Details:
- Payment QR code images are stored at `src/main/resources/static/images/pay/` with naming convention `{payType}_{sellerId}.{extension}`.
- Default QR codes are generated at application startup if they don't exist.
- The payment process is simulated through client-side actions without requiring actual payment processors.
- Payment success is recorded in the database, updating order status appropriately.

### Related Files:
- Frontend:
  - `frontend/src/views/cart/Payment.vue` - Main payment page with QR code display
  - `frontend/src/views/cart/PaymentSuccess.vue` - Success page after payment
  - `frontend/src/views/cart/Checkout.vue` - Updated to redirect to payment page
  - `frontend/src/views/seller/Apply.vue` - Updated to allow QR code upload during registration
  - `frontend/src/views/seller/Payment.vue` - Management of payment QR codes
  - `frontend/src/api/seller.ts` - API functions for QR code uploads
  - `frontend/src/router/index.ts` - Updated routes for payment pages

- Backend:
  - `src/main/java/com/shop/online/controller/SellerController.java` - Endpoints for QR code uploads
  - `src/main/java/com/shop/online/service/impl/SellerServiceImpl.java` - Implementation of QR code storage
  - `src/main/java/com/shop/online/util/FileUtil.java` - File handling utilities for QR codes
  - `src/main/java/com/shop/online/config/ApplicationStartupListener.java` - Default QR code initialization
