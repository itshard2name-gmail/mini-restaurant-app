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
      name: 'sub-app-admin',
      filename: 'remoteEntry.js',
      exposes: {
        './AdminDashboard': './src/components/AdminDashboard.vue',
      },
      shared: ['vue']
    })
  ],
  server: {
    port: 5003, // Run on a different port than host (5000) and other subs (5001, 5002)
    cors: true
  },
  preview: {
    port: 5003,
    cors: true,
    strictPort: true
  },
  build: {
    target: 'esnext', // Top-level await support
    cssCodeSplit: false,
    assetsDir: ''
  },
  // Ensure assets are loaded from the correct remote domain/port
  base: 'http://localhost:5003/'
})
