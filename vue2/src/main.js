import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui'  // 引入完整 ElementUI
import 'element-ui/lib/theme-chalk/index.css'  // 引入完整样式

Vue.use(ElementUI)  // 全局使用
Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')