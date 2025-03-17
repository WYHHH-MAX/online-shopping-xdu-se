# 订单明细表设计说明

## 表结构

以下是订单明细表的数据库设计：

```sql
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
```

## 必要性说明

订单明细表在电商系统中是必要的，原因如下：

1. **数据结构需求**: 订单与商品是多对多关系，一个订单可以包含多个商品，每个商品也可以出现在多个订单中。订单明细表作为中间表存储这种关系。

2. **数据完整性**: 订单创建后，即使商品信息（如名称、价格）发生变化，订单明细中的信息应保持不变，以确保历史订单的准确性。

3. **业务需求**: 需要记录每个商品的购买数量、单价和小计金额，这些信息需要单独存储。

4. **查询效率**: 通过订单明细表可以快速获取订单中的商品信息，而不需要复杂的关联查询。

5. **库存管理**: 创建订单时需要扣减库存，取消订单时需要恢复库存，这些操作需要知道每个商品的购买数量。

## 实现方式

在本项目中，我们使用以下方式实现订单明细功能：

1. **数据库表**: 使用`order_item`表存储订单明细信息。

2. **实体类**: 创建`OrderItemDTO`实体类对应数据库表结构。

3. **Mapper接口**: 使用`OrderItemMapper`接口定义对订单明细的操作。

4. **批量插入**: 通过XML配置实现批量插入订单明细的功能。

这种方式保持了代码结构的清晰，同时满足了业务需求。 