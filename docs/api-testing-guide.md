# API测试指南

## 订单API测试

### 获取订单列表

**请求URL:** `/api/orders`

**方法:** GET

**参数:**
```json
{
  "status": "0", // 可选，订单状态 (0-待付款, 1-待发货, 2-待收货, 3-已完成, 4-已取消)
  "page": 1,     // 可选，页码，默认为1
  "size": 10     // 可选，每页大小，默认为10
}
```

**Postman测试方法:**
1. 创建GET请求，URL设置为 `{{baseUrl}}/api/orders`
2. 在"Params"选项卡中添加以下参数：
   - status: 0 (可选，筛选订单状态)
   - page: 1
   - size: 10
3. 在"Headers"选项卡中添加:
   - Authorization: Bearer {{token}}

**响应示例:**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "orderNo": "202309170001",
        "status": "0",
        "totalAmount": 299.99,
        "products": [
          {
            "id": 12,
            "name": "商品名称",
            "image": "/images/product.jpg",
            "price": 299.99,
            "quantity": 1
          }
        ],
        "createdTime": "2023-09-17 10:00:00"
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 获取订单详情

**请求URL:** `/api/orders/{orderNo}`

**方法:** GET

**参数:** 路径参数 orderNo

**Postman测试方法:**
1. 创建GET请求，URL设置为 `{{baseUrl}}/api/orders/202309170001` (替换为实际订单号)
2. 在"Headers"选项卡中添加:
   - Authorization: Bearer {{token}}

### 创建订单

**请求URL:** `/api/orders/create`

**方法:** POST

**参数:**
```json
{
  "cartItemIds": [1, 2, 3] // 购物车项ID数组
}
```

**Postman测试方法:**
1. 创建POST请求，URL设置为 `{{baseUrl}}/api/orders/create`
2. 在"Body"选项卡中选择"raw"和"JSON"
3. 输入请求体:
   ```json
   {
     "cartItemIds": [1, 2, 3]
   }
   ```
4. 在"Headers"选项卡中添加:
   - Content-Type: application/json
   - Authorization: Bearer {{token}}

### 支付订单

**请求URL:** `/api/orders/pay`

**方法:** POST

**参数:**
```json
{
  "orderNo": "202309170001"
}
```

**Postman测试方法:**
1. 创建POST请求，URL设置为 `{{baseUrl}}/api/orders/pay`
2. 在"Body"选项卡中选择"raw"和"JSON"
3. 输入请求体:
   ```json
   {
     "orderNo": "202309170001"
   }
   ```
4. 在"Headers"选项卡中添加:
   - Content-Type: application/json
   - Authorization: Bearer {{token}}

### 取消订单

**请求URL:** `/api/orders/{orderNo}/cancel`

**方法:** POST

**参数:** 路径参数 orderNo

**Postman测试方法:**
1. 创建POST请求，URL设置为 `{{baseUrl}}/api/orders/202309170001/cancel` (替换为实际订单号)
2. 在"Headers"选项卡中添加:
   - Authorization: Bearer {{token}}

### 确认收货

**请求URL:** `/api/orders/{orderNo}/confirm`

**方法:** POST

**参数:** 路径参数 orderNo

**Postman测试方法:**
1. 创建POST请求，URL设置为 `{{baseUrl}}/api/orders/202309170001/confirm` (替换为实际订单号)
2. 在"Headers"选项卡中添加:
   - Authorization: Bearer {{token}}

## 环境设置

在Postman中创建环境变量:
- baseUrl: 你的API基础URL (例如 http://localhost:8080/api)
- token: 通过登录API获取的认证token 