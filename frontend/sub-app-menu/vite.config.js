import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import federation from '@originjs/vite-plugin-federation'
import path from 'path'

export default defineConfig({
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src')
        }
    },
    plugins: [
        vue(),
        federation({
            name: 'sub-app-menu',
            filename: 'remoteEntry.js',
            exposes: {
                './MenuList': './src/components/MenuList.vue',
                './MyOrders': './src/components/MyOrders.vue',
                './MenuPage': './src/components/MenuPage.vue'
            },
            shared: ['vue', 'pinia', 'vue-router']
        })
    ],
    build: {
        target: 'esnext',
        cssCodeSplit: false,
    },
    server: {
        port: process.env.PORT_FRONTEND_MENU || 5001,
        proxy: {
            '/api': {
                target: `http://localhost:${process.env.PORT_GATEWAY || 8088}`,
                changeOrigin: true
            }
        }
    },
    preview: {
        port: process.env.PORT_FRONTEND_MENU || 5001,
        strictPort: true
    }
})
