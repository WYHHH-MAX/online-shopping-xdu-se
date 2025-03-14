<template>
  <div class="checkout-page">
    <h1>结算页面</h1>
    <p>该功能尚未实现，敬请期待。</p>
    <a-button type="primary" @click="goBack">返回</a-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

const goBack = () => {
  router.back();
};
</script>

<style scoped>
.checkout-page {
  padding: 24px;
  text-align: center;
}
</style> 