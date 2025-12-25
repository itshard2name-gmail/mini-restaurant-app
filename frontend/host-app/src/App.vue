<script setup>
import ErrorBoundary from './components/ErrorBoundary.vue';
import { ref, watch, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const isAdmin = ref(false);
const isMenuOpen = ref(false);

const handleLogout = () => {
  localStorage.clear(); // Clear token AND cart state
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
const token = ref(localStorage.getItem('token'));

watch(() => route.path, () => {
  token.value = localStorage.getItem('token');
  checkRole();
});

onMounted(() => {
  checkRole();
});
</script>

<template>
  <div class="bg-gray-100 min-h-screen">
    <nav class="bg-gradient-to-r from-orange-500 to-red-600 shadow-lg p-4 mb-4 sticky top-0 z-[500] w-full">
      <div class="max-w-7xl mx-auto flex justify-between items-center">
        <div class="flex items-center gap-3">
          <!-- Logo Icon -->
          <div class="bg-white/20 p-2 rounded-lg backdrop-blur-sm">
             <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
          </div>
          <div class="font-extrabold text-2xl text-white tracking-tight">Restaurant App</div>
        </div>
        
        <!-- Desktop Menu -->
        <!-- Desktop Menu -->
        <div class="hidden lg:block">
            <div class="flex items-center gap-4">
              <!-- Menu: Visible to ALL (Admin & User) -->
              <router-link to="/menu" class="text-white/90 hover:text-white font-medium px-3 py-2 rounded-md hover:bg-white/10 transition-colors" active-class="bg-white/20 text-white">
                 Menu
              </router-link>
              
              <!-- My Orders: Only for USER -->
              <router-link v-if="!isAdmin" to="/my-orders" class="text-white/90 hover:text-white font-medium px-3 py-2 rounded-md hover:bg-white/10 transition-colors" active-class="bg-white/20 text-white">
                 My Orders
              </router-link>

              <!-- Admin Dashboard: Only for ADMIN -->
              <router-link v-if="isAdmin" to="/admin" class="text-white/90 hover:text-white font-medium px-3 py-2 rounded-md hover:bg-white/10 transition-colors flex items-center gap-2" active-class="bg-white/20 text-white">
                 <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg>
                 Admin Dashboard
              </router-link>
              
              <div class="h-6 w-px bg-white/20 mx-2"></div>
              
              <router-link v-if="!token" to="/login" class="text-white/80 hover:text-white text-sm font-medium hover:underline">
                Sign In
              </router-link>
              <button v-else @click="handleLogout" class="text-white/80 hover:text-white text-sm font-medium hover:underline">
                Logout
              </button>
            </div>
        </div>

        <!-- Mobile Menu Button -->
        <div class="lg:hidden flex items-center">
            <button @click="isMenuOpen = !isMenuOpen" class="text-white hover:bg-white/20 p-2 rounded-md transition-colors">
                <svg v-if="!isMenuOpen" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path></svg>
                <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
        </div>
      </div>

      <!-- Mobile Menu Dropdown -->
      <div v-show="isMenuOpen" class="lg:hidden mt-4 pt-4 border-t border-white/20 flex flex-col gap-2">
          <router-link to="/menu" class="text-white hover:bg-white/10 px-4 py-3 rounded-md font-medium" active-class="bg-white/20" @click="isMenuOpen = false">Menu</router-link>
          
          <router-link v-if="!isAdmin" to="/my-orders" class="text-white hover:bg-white/10 px-4 py-3 rounded-md font-medium" active-class="bg-white/20" @click="isMenuOpen = false">My Orders</router-link>
          
          <router-link v-if="isAdmin" to="/admin" class="text-white hover:bg-white/10 px-4 py-3 rounded-md font-medium flex items-center gap-2" active-class="bg-white/20" @click="isMenuOpen = false">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg>
              Admin Dashboard
          </router-link>
          
          <div class="h-px bg-white/20 mx-2 my-2"></div>
          
          <router-link v-if="!token" to="/login" class="text-left text-white/90 hover:bg-white/10 px-4 py-3 rounded-md font-medium" @click="isMenuOpen = false">
             Sign In
          </router-link>
          <button v-else @click="() => { isMenuOpen = false; handleLogout(); }" class="text-left text-white/90 hover:bg-white/10 px-4 py-3 rounded-md font-medium">
             Logout
          </button>
      </div>

    </nav>
    <ErrorBoundary>
        <router-view :key="$route.fullPath"></router-view>
    </ErrorBoundary>
  </div>
</template>
