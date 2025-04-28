<template>
  <div class="order-actions-container">
    <!-- 待付款状态 -->
    <template v-if="statusNumber === ORDER_STATUS.PENDING_PAYMENT">
      <a-button type="primary" @click="handlePay">Pay now</a-button>
      <a-button @click="handleCancel">Cancel order</a-button>
    </template>
    
    <!-- 待发货状态 -->
    <template v-if="statusNumber === ORDER_STATUS.PENDING_SHIPMENT">
      <a-button @click="handleCancel">Cancel order</a-button>
    </template>
    
    <!-- 待收货状态 -->
    <template v-if="statusNumber === ORDER_STATUS.PENDING_RECEIPT">
      <a-button type="primary" @click="handleConfirm">Confirm receipt</a-button>
    </template>
    
    <!-- 已完成状态 -->
    <template v-if="statusNumber === ORDER_STATUS.COMPLETED">
      <a-button type="primary" @click="handleReview">Review</a-button>
      <a-button type="danger" @click="handleRefund">Apply for refund</a-button>
    </template>
    
    <!-- 已取消状态 -->
    <template v-if="statusNumber === ORDER_STATUS.CANCELLED">
      <slot name="cancelled"></slot>
    </template>
    
    <!-- 已退款状态 -->
    <template v-if="statusNumber === ORDER_STATUS.REFUNDED">
      <slot name="refunded"></slot>
    </template>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, defineEmits, computed } from 'vue';
import { Modal, message } from 'ant-design-vue';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import { h } from 'vue';
import { payOrder, cancelOrder, confirmOrder, refundOrder } from '@/api/order';

// 订单状态常量
const ORDER_STATUS = {
  PENDING_PAYMENT: 0,  // 待付款
  PENDING_SHIPMENT: 1, // 待发货
  PENDING_RECEIPT: 2,  // 待收货
  COMPLETED: 3,        // 已完成
  CANCELLED: 4,        // 已取消
  REFUNDED: 5          // 已退款
};

// 组件属性
const props = defineProps<{
  status: string | number;
  orderNo: string;
}>();

// 计算属性，将status转换为数字
const statusNumber = computed(() => {
  return typeof props.status === 'string' ? parseInt(props.status) : props.status;
});

// 定义事件
const emit = defineEmits<{
  (e: 'refreshOrders'): void;
  (e: 'pay', orderNo: string): void;
  (e: 'review', orderNo: string): void;
}>();

// 处理支付
const handlePay = () => {
  emit('pay', props.orderNo);
};

// 处理取消订单
const handleCancel = () => {
  Modal.confirm({
    title: '确认取消订单',
    icon: () => h(ExclamationCircleOutlined),
    content: '确定要取消这个订单吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await cancelOrder(props.orderNo);
        message.success('订单已取消');
        emit('refreshOrders');
      } catch (error) {
        console.error('取消订单失败:', error);
        message.error('取消订单失败');
      }
    }
  });
};

// 处理确认收货
const handleConfirm = () => {
  Modal.confirm({
    title: '确认收货',
    icon: () => h(ExclamationCircleOutlined),
    content: '确认已收到商品吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await confirmOrder(props.orderNo);
        message.success('已确认收货');
        emit('refreshOrders');
      } catch (error) {
        console.error('确认收货失败:', error);
        message.error('确认收货失败');
      }
    }
  });
};

// 处理评价
const handleReview = () => {
  emit('review', props.orderNo);
};

// 处理退款
const handleRefund = () => {
  Modal.confirm({
    title: '确认申请退款',
    icon: () => h(ExclamationCircleOutlined),
    content: '确定要申请退款吗？此操作无法撤销。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await refundOrder(props.orderNo);
        message.success('退款申请成功');
        emit('refreshOrders');
      } catch (error) {
        console.error('申请退款失败:', error);
        message.error('申请退款失败');
      }
    }
  });
};
</script>

<style scoped>
.order-actions-container {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style> 