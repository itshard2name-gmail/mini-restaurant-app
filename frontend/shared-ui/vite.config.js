import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import { resolve } from 'node:path'

export default defineConfig({
    plugins: [vue()],
    build: {
        lib: {
            entry: fileURLToPath(new URL('./src/index.js', import.meta.url)),
            name: 'MiniRestaurantUI',
            fileName: 'mini-restaurant-ui'
        },
        rollupOptions: {
            external: ['vue'],
            output: {
                globals: {
                    vue: 'Vue'
                }
            }
        }
    },
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    }
})
