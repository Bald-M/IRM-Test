// Import global styles
import '@/assets/main.css'
import 'element-plus/dist/index.css'
import '@/assets/theme.css'
// Import Element Plus icon library
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import ElementPlus from 'element-plus'
// Import custom axios instance and utility functions
import axios from './utils/http'
import watcher from './utils/watchDog'
import setupRouterGuard from './utils/permission'
// Import Vue modules and project files
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue' // Root component
import router from './router' // Project router

// Create the Vue application
const app = createApp(App)

// Register Element Plus icons globally
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// Make the custom axios instance globally available
app.config.globalProperties.$axios = axios
app.provide('$axios', axios)

// Initialize plugins and mount the application
app.use(ElementPlus) // Use Element Plus plugin
app.use(createPinia()) // Use Pinia for state management
setupRouterGuard() // Register guards before router installation starts the initial navigation
app.use(router) // Use the router
app.mount('#app') // Mount the app to the DOM

watcher(1) 
