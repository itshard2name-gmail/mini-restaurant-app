<template>
  <div class="p-8 bg-gray-100 min-h-screen font-sans">
    <div class="max-w-7xl mx-auto">
      
      <!-- Top Navigation Header -->
      <div class="flex flex-col md:flex-row items-center justify-between mb-8 pb-6 border-b border-gray-200">
        <div>
          <h1 class="text-3xl font-extrabold text-gray-900 tracking-tight">Admin Dashboard</h1>
          <p class="mt-2 text-sm text-gray-500">Manage your restaurant operations</p>
        </div>
        
        <!-- Navigation Tabs -->
        <div class="mt-4 md:mt-0 bg-white p-1 rounded-lg border border-gray-200 shadow-sm flex space-x-1">
           <button 
             @click="currentTab = 'orders'"
             :class="['px-4 py-2 text-sm font-medium rounded-md transition-all duration-200', 
                      currentTab === 'orders' ? 'bg-indigo-50 text-indigo-700 shadow-sm' : 'text-gray-500 hover:text-gray-900 hover:bg-gray-50']"
           >
             Active Orders
           </button>
           <button 
             @click="currentTab = 'menu'"
             :class="['px-4 py-2 text-sm font-medium rounded-md transition-all duration-200', 
                      currentTab === 'menu' ? 'bg-indigo-50 text-indigo-700 shadow-sm' : 'text-gray-500 hover:text-gray-900 hover:bg-gray-50']"
           >
             Menu Management
           </button>
        </div>
      </div>
      
      <!-- Content Area -->
      <transition 
         enter-active-class="transition ease-out duration-200" 
         enter-from-class="opacity-0 translate-y-2" 
         enter-to-class="opacity-100 translate-y-0"
         leave-active-class="transition ease-in duration-150" 
         leave-from-class="opacity-100 translate-y-0" 
         leave-to-class="opacity-0 translate-y-2"
         mode="out-in"
      >
        <component :is="activeComponent" />
      </transition>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import '../style.css'; 

import OrderManagement from './OrderManagement.vue';
import MenuManagement from './MenuManagement.vue';

const currentTab = ref('orders');

const activeComponent = computed(() => {
  return currentTab.value === 'orders' ? OrderManagement : MenuManagement;
});
</script>
