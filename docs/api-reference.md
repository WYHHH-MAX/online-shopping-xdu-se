# API 参考文档

## 基础设置

### 后端API基础路径
所有API请求路径前缀为：`/api`

### 前端配置
前端baseURL配置为：`/api`，在`frontend/src/utils/request.ts`中设置。

## 后端API路径

### 认证相关 (/auth)
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 购物车相关 (/cart)
- `GET /api/cart/list` - 获取购物车列表
- `POST /api/cart/add` - 添加商品到购物车
- `POST /api/cart/quickAdd` - 快速添加商品到购物车（数量固定为1）
- `PUT /api/cart/update` - 更新购物车商品数量
- `DELETE /api/cart/delete/{id}` - 删除购物车商品
- `PUT /api/cart/select` - 选中/取消选中购物车商品
- `POST /api/cart/selectBatch` - 批量选择购物车商品
- `PUT /api/cart/selectAll` - 全选/取消全选购物车商品
- `GET /api/cart/count` - 获取购物车商品数量

### 分类相关 (/public/category)
- `GET /api/public/category/tree` - 获取分类树
- `GET /api/public/category/list` - 获取分类列表
- `POST /api/public/category` - 创建分类
- `PUT /api/public/category` - 更新分类
- `DELETE /api/public/category/{id}` - 删除分类
- `GET /api/public/category/{id}` - 获取分类详情

### 商品相关 (/product)
- `GET /api/product/category/{categoryId}` - 获取指定分类的商品列表
- `GET /api/product/{id}` - 获取商品详情
- `GET /api/product/search` - 搜索商品
- `GET /api/product/seller/{sellerId}` - 获取指定卖家的商品列表
- `GET /api/product/featured` - 获取推荐商品

### 订单相关 (/orders)
- `GET /api/orders` - 获取订单列表
- `POST /api/orders/create` - 创建订单
- `POST /api/orders/pay` - 支付订单
- `POST /api/orders/{orderNo}/cancel` - 取消订单
- `POST /api/orders/{orderNo}/confirm` - 确认收货
- `GET /api/orders/{orderNo}` - 获取订单详情

### 卖家相关 (/seller)
- `GET /api/seller/{userId}` - 获取卖家信息
- `POST /api/seller` - 创建卖家
- `PUT /api/seller` - 更新卖家信息

### 其他公共接口 (/public)
- `GET /api/public/test` - 测试接口
- `GET /api/public/health` - 健康检查
- `GET /api/public/version` - 获取版本信息

## 前端API调用

### 认证相关
```typescript
// 登录
login(username: string, password: string)

// 注册
register(data: RegisterData)
```

### 购物车相关
```typescript
// 获取购物车列表
getCartItems()

// 添加商品到购物车
addToCart(productId: number, quantity: number)

// 快速添加商品到购物车
quickAddToCart(productId: number)

// 更新购物车商品数量
updateCartItem(id: number, quantity: number)

// 删除购物车商品
deleteCartItem(id: number)

// 选中/取消选中购物车商品
selectCartItem(id: number, selected: boolean)

// 批量选择购物车商品
selectCartItems(ids: number[])

// 全选/取消全选购物车商品
selectAllCartItems(selected: boolean)

// 获取购物车商品数量
getCartCount()
```

### 订单相关
```typescript
// 获取订单列表
getOrders(params: OrderQuery)

// 获取订单详情
getOrderDetail(orderNo: string)

// 创建订单
createOrder(data: { cartItemIds: number[] })

// 支付订单
payOrder(orderNo: string)

// 取消订单
cancelOrder(orderNo: string)

// 确认收货
confirmOrder(orderNo: string)
```

### 商品相关
```typescript
// 获取商品列表
getProducts(params: ProductQuery)

// 获取商品详情
getProductDetail(id: number)

// 搜索商品
searchProducts(keyword: string)

// 获取推荐商品
getFeaturedProducts()
```

### 分类相关
```typescript
// 获取分类树
getCategoryTree()

// 获取分类列表
getCategoryList()
```

## 数据模型

### 用户 (User)
```typescript
interface User {
  id: number;
  username: string;
  nickname: string;
  role: number; // 0-买家，1-卖家
}
```

### 商品 (Product)
```typescript
interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  mainImage: string;
  images: string[];
  categoryId: number;
  sellerId: number;
}
```

### 订单 (Order)
```typescript
interface Order {
  id: number;
  orderNo: string;
  userId: number;
  status: number; // 0-待付款，1-待发货，2-待收货，3-已完成，4-已取消
  totalAmount: number;
  products: OrderProduct[];
}
```

### 购物车 (Cart)
```typescript
interface CartItem {
  id: number;
  userId: number;
  productId: number;
  quantity: number;
  selected: boolean;
  product: Product;
}
``` 