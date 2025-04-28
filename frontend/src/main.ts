import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router/index'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/antd.css'
// import './assets/main.css'
// import './assets/main.css'
import orderActionsPlugin from './plugins/orderActionsPlugin'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(Antd)
app.use(orderActionsPlugin)

app.mount('#app')
