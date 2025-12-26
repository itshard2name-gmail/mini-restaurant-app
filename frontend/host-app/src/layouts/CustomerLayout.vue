<script setup>
import { ref } from 'vue';
import { useAuth } from '../composables/useAuth';

const { token, isAdmin, handleLogout } = useAuth();
const isMenuOpen = ref(false);
</script>

<template>
  <div class="bg-gray-100 min-h-screen">
    <!-- Customer Navbar -->
    <nav class="bg-gradient-to-r from-orange-500 to-red-600 shadow-lg p-4 mb-4 sticky top-0 z-[500] w-full">
      <div class="max-w-7xl mx-auto flex justify-between items-center">
        <div class="flex items-center gap-3">
          <!-- Logo -->
          <div class="bg-white/20 p-2 rounded-lg backdrop-blur-sm">
             <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
          </div>
          <div class="font-extrabold text-2xl text-white tracking-tight">Restaurant App</div>
        </div>
        
        <!-- Desktop Menu -->
        <div class="hidden lg:block">
            <div class="flex items-center gap-4">
              <router-link to="/menu" class="text-white/90 hover:text-white font-medium px-3 py-2 rounded-md hover:bg-white/10 transition-colors" active-class="bg-white/20 text-white">
                 Menu
              </router-link>
              
              <router-link v-if="!isAdmin && token" to="/my-orders" class="text-white/90 hover:text-white font-medium px-3 py-2 rounded-md hover:bg-white/10 transition-colors" active-class="bg-white/20 text-white">
                 My Orders
              </router-link>

              <!-- Cross-link for Admin who happens to be viewing Menu -->
              <router-link v-if="isAdmin" to="/admin" class="text-white/90 hover:text-white font-medium px-3 py-2 rounded-md hover:bg-white/10 transition-colors flex items-center gap-2">
                 <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                 Dashboard
              </router-link>
              
              <div class="h-6 w-px bg-white/20 mx-2"></div>
              
              <div class="flex items-center">
                  <router-link v-if="!token" to="/login" class="text-white/80 hover:text-white text-sm font-medium hover:underline">
                    Sign In
                  </router-link>
                  <button v-else @click="handleLogout" class="text-white/80 hover:text-white text-sm font-medium hover:underline">
                    Logout
                  </button>
              </div>
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
          
          <router-link v-if="!isAdmin && token" to="/my-orders" class="text-white hover:bg-white/10 px-4 py-3 rounded-md font-medium" active-class="bg-white/20" @click="isMenuOpen = false">My Orders</router-link>
          
          <router-link v-if="isAdmin" to="/admin" class="text-white hover:bg-white/10 px-4 py-3 rounded-md font-medium flex items-center gap-2" active-class="bg-white/20" @click="isMenuOpen = false">
              Dashboard
          </router-link>
          
          <div class="h-px bg-white/20 mx-2 my-2"></div>
          
          <div>
              <router-link v-if="!token" to="/login" class="text-left text-white/90 hover:bg-white/10 px-4 py-3 rounded-md font-medium block" @click="isMenuOpen = false">
                 Sign In
              </router-link>
              <button v-else @click="() => { isMenuOpen = false; handleLogout(); }" class="text-left w-full text-white/90 hover:bg-white/10 px-4 py-3 rounded-md font-medium block">
                 Logout
              </button>
          </div>
       </div>
    </nav>
    <slot />
  </div>
</template>
