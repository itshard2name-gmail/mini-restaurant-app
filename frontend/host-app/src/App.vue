<script setup>
import ErrorBoundary from './components/ErrorBoundary.vue';
import { ref, watch, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const isAdmin = ref(false);

const handleLogout = () => {
  localStorage.removeItem('token');
  router.push('/login');
};

const checkRole = () => {
  const token = localStorage.getItem('token');
  if (!token) {
    isAdmin.value = false;
    return;
  }
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    isAdmin.value = payload.roles && payload.roles.includes('ROLE_ADMIN');
  } catch (e) {
    isAdmin.value = false;
  }
};

watch(() => route.path, () => {
  checkRole();
});

onMounted(() => {
  checkRole();
});
</script>

<template>
  <div class="bg-gray-100 min-h-screen">
    <nav class="bg-gradient-to-r from-orange-500 to-red-600 shadow-lg p-4 mb-4" v-if="$route.path !== '/login'">
      <div class="max-w-7xl mx-auto flex justify-between items-center">
        <div class="flex items-center gap-3">
          <!-- Logo Icon -->
          <div class="bg-white/20 p-2 rounded-lg backdrop-blur-sm">
             <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
          </div>
          <div class="font-extrabold text-2xl text-white tracking-tight">Restaurant App</div>
        </div>
        
        <div class="flex items-center gap-4">
          <!-- Menu: Visible to ALL (Admin & User) -->
          <router-link to="/menu" class="text-white/90 hover:text-white font-medium px-3 py-2 rounded-md hover:bg-white/10 transition-colors" active-class="bg-white/20 text-white">
             Orders
          </router-link>
          
          <!-- My Orders: Only for USER -->
          <router-link v-if="!isAdmin" to="/orders" class="text-white/90 hover:text-white font-medium px-3 py-2 rounded-md hover:bg-white/10 transition-colors" active-class="bg-white/20 text-white">
             My Orders
          </router-link>

          <!-- Admin Dashboard: Only for ADMIN -->
          <router-link v-if="isAdmin" to="/admin" class="text-white/90 hover:text-white font-medium px-3 py-2 rounded-md hover:bg-white/10 transition-colors flex items-center gap-2" active-class="bg-white/20 text-white">
             <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg>
             Admin Dashboard
          </router-link>
          
          <div class="h-6 w-px bg-white/20 mx-2"></div>
          
          <button @click="handleLogout" class="text-white/80 hover:text-white text-sm font-medium hover:underline">
            Logout
          </button>
        </div>
      </div>
    </nav>
    <ErrorBoundary>
        <router-view></router-view>
    </ErrorBoundary>
  </div>
</template>
