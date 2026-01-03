import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import federation from '@originjs/vite-plugin-federation'

import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  plugins: [
    vue(),
    federation({
      name: 'host-app',
      remotes: {
        'sub-app-menu': `http://localhost:${process.env.PORT_FRONTEND_MENU || 10001}/assets/remoteEntry.js`,
        'sub-app-admin': `http://localhost:${process.env.PORT_FRONTEND_ADMIN || 10002}/remoteEntry.js`,
      },
      shared: ['vue', 'pinia', 'vue-router']
    })
  ],
  build: {
    target: 'esnext' // Required for federation
  },
  server: {
    port: process.env.PORT_FRONTEND_HOST || 3000,
    proxy: {
      '/api': {
        target: `http://localhost:${process.env.PORT_ENVOY || 8888}`,
        changeOrigin: true
      }
    }
  },
  preview: {
    port: process.env.PORT_FRONTEND_HOST || 3000,
    strictPort: true
  }
})
