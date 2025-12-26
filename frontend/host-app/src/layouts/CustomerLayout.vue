<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useAuth } from '../composables/useAuth';
import { useCustomerTheme } from '../composables/useCustomerTheme';

import { useSettings } from '../composables/useSettings';

const { token, isAdmin, handleLogout } = useAuth();
const { initTheme, setTheme, clearTheme, currentTheme, themes } = useCustomerTheme();
const { settings, fetchSettings } = useSettings();

const isMenuOpen = ref(false);
const isThemeMenuOpen = ref(false);

onMounted(() => {
    initTheme();
    fetchSettings();
});

onUnmounted(() => {
    clearTheme();
});
</script>

<template>
  <div class="bg-[hsl(var(--background))] text-[hsl(var(--foreground))] min-h-screen transition-colors duration-300">
    <!-- Customer Navbar -->
    <nav class="bg-[hsl(var(--primary))] shadow-lg p-4 sticky top-0 z-[500] w-full transition-colors duration-300">
      <div class="max-w-7xl mx-auto flex justify-between items-center">
        <div class="flex items-center gap-3">
          <!-- Logo -->
          <div class="bg-white/20 p-2 rounded-lg backdrop-blur-sm">
             <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
          </div>
          <div class="font-extrabold text-2xl text-white tracking-tight">{{ settings.BRAND_NAME }}</div>
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
              
               <!-- Theme Dropdown -->
               <div class="relative">
                   <button @click="isThemeMenuOpen = !isThemeMenuOpen" class="text-white/80 hover:text-white flex items-center gap-1.5 px-3 py-2 rounded-md hover:bg-white/10 transition-colors">
                       <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01"></path></svg>
                       <span class="text-sm font-medium">Theme</span>
                   </button>
                   
                   <div v-if="isThemeMenuOpen" class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50 ring-1 ring-black ring-opacity-5">
                       <button 
                           v-for="theme in themes" 
                           :key="theme.id"
                           @click="setTheme(theme.id); isThemeMenuOpen = false"
                           class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center justify-between"
                           :class="currentTheme === theme.id ? 'text-orange-600 font-bold bg-orange-50' : 'text-gray-700'"
                       >
                           {{ theme.name }}
                           <svg v-if="currentTheme === theme.id" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                       </button>
                   </div>
                   <!-- Backdrop to close -->
                    <div v-if="isThemeMenuOpen" @click="isThemeMenuOpen = false" class="fixed inset-0 z-40"></div>
               </div>

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

           <!-- Theme Mobile -->
          <div class="px-4 py-2 border-t border-white/10 mt-2">
              <span class="text-xs uppercase text-white/50 font-bold tracking-wider mb-2 block">Theme</span>
              <div class="grid grid-cols-3 gap-2">
                  <button 
                       v-for="theme in themes" 
                       :key="theme.id"
                       @click="setTheme(theme.id); isMenuOpen = false"
                       class="text-center px-2 py-1.5 rounded text-xs font-bold border border-white/20"
                       :class="currentTheme === theme.id ? 'bg-white text-orange-600' : 'text-white hover:bg-white/10'"
                  >
                      {{ theme.name }}
                  </button>
              </div>
          </div>
          
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
