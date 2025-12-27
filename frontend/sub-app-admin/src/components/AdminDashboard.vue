<template>
  <!-- Isolation Wrapper: Inherit Dark Mode -->
  <div id="admin-wrapper" class="admin-scope text-foreground bg-background"> <!-- "Fake Body" styles applied here -->    
    <!-- Main Container -->
    <div id="sub-app-admin" class="bg-background min-h-screen font-sans text-foreground">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        
        <!-- Top Navigation Header -->
        <div class="flex flex-col md:flex-row items-center justify-between mb-8 pb-6 border-b border-border">
          <div>
            <h1 class="text-3xl font-extrabold text-foreground tracking-tight">Admin Dashboard</h1>
            <p class="mt-2 text-sm text-muted-foreground">Manage your restaurant operations</p>
          </div>
          
          <!-- Navigation Tabs -->
          <div class="mt-4 md:mt-0 bg-muted p-1.5 rounded-lg border border-border shadow-sm flex space-x-1">
             <button 
               v-for="nav in navigation" 
               :key="nav.name"
               @click="currentTab = nav.name"
               :class="['px-4 py-2 text-sm font-medium rounded-md transition-all duration-200', 
                        currentTab === nav.name ? 'bg-background text-primary shadow-sm border border-border' : 'text-muted-foreground hover:text-foreground hover:bg-background/50']"
             >
               {{ nav.label }}
             </button>
          </div>
        </div>
        
        <!-- Main Content Area -->
        <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          
           <!-- Component View -->
           <transition name="fade" mode="out-in">
              <KeepAlive>
                 <component :is="currentComponent" />
              </KeepAlive>
           </transition>
  
        </main>
      </div>
    </div>

  </div> 
</template>

<script setup>
import { ref, computed, defineAsyncComponent } from 'vue';

import '../style.css';

// Async Components
const OrderManagement = defineAsyncComponent(() => import('./OrderManagement.vue'));
const MenuManagement = defineAsyncComponent(() => import('./MenuManagement.vue'));
const AnalyticsBoard = defineAsyncComponent(() => import('./AnalyticsBoard.vue'));
const SettingsBoard = defineAsyncComponent(() => import('./SettingsBoard.vue'));

const currentTab = ref('orders'); // Default to Orders for better operational flow

const navigation = [
  { name: 'orders', label: 'Active Orders', component: OrderManagement },
  { name: 'menu', label: 'Menu Management', component: MenuManagement },
  { name: 'analytics', label: 'Reports (BI)', component: AnalyticsBoard },
  { name: 'settings', label: 'Settings', component: SettingsBoard },
];

const currentComponent = computed(() => {
  const tab = navigation.find(n => n.name === currentTab.value);
  return tab ? tab.component : OrderManagement;
});
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
