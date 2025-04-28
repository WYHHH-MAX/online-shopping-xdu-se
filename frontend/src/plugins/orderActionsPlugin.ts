import { App } from 'vue';
import OrderActions from '@/components/OrderActions.vue';

export default {
  install: (app: App) => {
    // 全局注册OrderActions组件
    app.component('OrderActions', OrderActions);
  }
}; 