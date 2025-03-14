西电软工大三SPM线上管理项目前端spm2\frontend(持续更新中...)

## 安装和使用

### 前提条件
- Node.js (v14+)
- Java JDK (v8+)
- Maven
- MySQL
- Redis

### 环境配置
# 安装Node.js环境
访问 Node.js官网 下载安装最新LTS版本
# 验证安装: 
```bash
node -v
npm -v
```
# 后端部分
Java环境: 安装JDK 8或更高版本
验证安装: java -version
Maven: 安装最新版本的Maven
# 验证安装: 
```bash
mvn -v
```
数据库配置
安装MySQL (5.0/8.0)
安装Redis服务
### 前端设置
```bash
# 安装依赖
cd frontend
npm install

# 启动
npm run dev

# 构建生产版本
npm run build
```


### 后端设置
```bash
# 配置数据库
# 修改 src/main/resources/application.yml 中的数据库连接信息
# (5.0，如果是8.0需要修改  datasource:driver-class-name: com.mysql.jdbc.Driver)
# 启动MySQL和Redis服务

# 启动应用
mvn spring-boot:run
```
#(或者直接运行OnlineShopApplication类中的main方法)
## 贡献
欢迎提交问题和拉取请求。

# 电商平台项目

一个功能完善的电商平台，支持商品浏览、搜索、购物车、订单管理等功能。

## 技术栈

### 前端
- **框架**: Vue 3
- **UI组件库**: Ant Design Vue
- **状态管理**: Vue Composition API
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **CSS预处理器**: SCSS/CSS with scoped styling

### 后端
- **框架**: Spring Boot
- **ORM**: MyBatis-Plus
- **数据库**: MySQL
- **缓存**: Redis
- **认证授权**: Spring Security + JWT
- **API文档**: Swagger/OpenAPI

## 功能特点
- 商品分类浏览
- 商品搜索和筛选
- 商品详情页带放大镜功能
- 购物车管理
- 订单处理
- 用户认证和授权
- 个人中心和订单历史

## 项目结构
spm2/
├── frontend/ # Vue.js前端项目
│ ├── public/ # 静态资源
│ ├── src/ # 源代码
│ │ ├── api/ # API请求
│ │ ├── assets/ # 图片和样式资源
│ │ ├── components/ # Vue组件
│ │ ├── router/ # 路由配置
│ │ ├── store/ # 状态管理
│ │ ├── utils/ # 工具函数
│ │ ├── views/ # 页面组件
│ │ ├── App.vue # 根组件
│ │ └── main.js # 入口文件
│ └── package.json # 依赖和脚本
├── src/ # Java后端项目
│ ├── main/
│ │ ├── java/ # Java代码
│ │ │ └── com/example/ecommerce/
│ │ │ ├── config/ # 配置类
│ │ │ ├── controller/ # 控制器
│ │ │ ├── mapper/ # MyBatis映射
│ │ │ ├── model/ # 数据模型
│ │ │ ├── service/ # 服务层
│ │ │ └── Application.java # 启动类
│ │ └── resources/ # 配置文件和静态资源
│ │ ├── mapper/ # MyBatis XML映射
│ │ ├── static/ # 静态资源
│ │ └── application.yml # 应用配置
└── pom.xml # Maven配置
