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
        'sub-app-menu': 'http://localhost:5001/assets/remoteEntry.js',
        'sub-app-admin': 'http://localhost:5003/assets/remoteEntry.js',
      },
      shared: ['vue', 'pinia']
    })
  ],
  build: {
    target: 'esnext' // Required for federation
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true
      }
    }
  },
  preview: {
    port: 3000,
    strictPort: true
  }
})
