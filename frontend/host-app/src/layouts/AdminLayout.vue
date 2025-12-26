<script setup>
import { ref } from 'vue';
import { useAuth } from '../composables/useAuth';
import { useTheme } from '../composables/useTheme';

const { token, handleLogout } = useAuth();
const { isDark, toggleTheme } = useTheme();
const isMenuOpen = ref(false);
</script>

<template>
  <div class="bg-gray-100 dark:bg-gray-950 min-h-screen transition-colors duration-300">
    <!-- Admin Navbar -->
    <nav class="bg-white dark:bg-gray-900 shadow-md p-4 mb-4 sticky top-0 z-[500] w-full border-b border-gray-200 dark:border-gray-800 transition-colors duration-300">
      <div class="max-w-7xl mx-auto flex justify-between items-center">
        <div class="flex items-center gap-3">
          <!-- Logo -->
          <div class="bg-indigo-600 p-2 rounded-lg shadow-sm">
             <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg>
          </div>
          <div class="font-bold text-xl text-gray-900 dark:text-white tracking-tight">Admin Console</div>
        </div>
        
        <!-- Desktop Menu -->
        <div class="hidden lg:block">
            <div class="flex items-center gap-4">
              <!-- Theme Toggle -->
              <button 
                @click="toggleTheme" 
                class="p-2 rounded-full text-gray-500 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800 transition-colors focus:outline-none"
                :title="isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'"
              >
                <!-- Sun Icon -->
                <svg v-if="isDark" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
                <!-- Moon Icon -->
                <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
                </svg>
              </button>

              <router-link to="/menu" class="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white font-medium px-3 py-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
                 Storefront (Menu)
              </router-link>
              
              <div class="h-6 w-px bg-gray-300 dark:bg-gray-700 mx-2"></div>
              
              <div class="flex items-center">
                  <span class="text-gray-500 dark:text-gray-400 text-sm mr-4">Administrator</span>
                  <button @click="handleLogout" class="text-red-600 hover:text-red-700 bg-red-100 px-3 py-1 rounded-md text-sm font-medium hover:bg-red-200 dark:bg-red-900/20 dark:text-red-200 dark:hover:bg-red-900/40 transition-colors">
                    Logout
                  </button>
              </div>
            </div>
        </div>

        <!-- Mobile Menu Button -->
        <div class="lg:hidden flex items-center gap-4">
            <button 
                @click="toggleTheme" 
                class="p-2 rounded-full text-gray-500 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800 transition-colors"
            >
                <svg v-if="isDark" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>
            </button>
            <button @click="isMenuOpen = !isMenuOpen" class="text-gray-600 dark:text-white hover:bg-gray-100 dark:hover:bg-gray-800 p-2 rounded-md transition-colors">
                <svg v-if="!isMenuOpen" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path></svg>
                <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
        </div>
      </div>

      <!-- Mobile Menu Dropdown -->
      <div v-show="isMenuOpen" class="lg:hidden mt-4 pt-4 border-t border-gray-200 dark:border-gray-700 flex flex-col gap-2">
          <router-link to="/menu" class="text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 px-4 py-3 rounded-md font-medium" @click="isMenuOpen = false">Back to Menu</router-link>
          
          <div class="h-px bg-gray-200 dark:bg-gray-700 mx-2 my-2"></div>
          
          <button @click="() => { isMenuOpen = false; handleLogout(); }" class="text-left w-full text-red-600 dark:text-red-300 hover:bg-red-50 dark:hover:bg-red-900/20 px-4 py-3 rounded-md font-medium block">
              Logout
          </button>
       </div>
    </nav>
    <slot />
  </div>
</template>
