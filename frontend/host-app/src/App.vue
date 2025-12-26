<script setup>
import ErrorBoundary from './components/ErrorBoundary.vue';
import { ref, watch, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

// Use useStorage for reactive local storage
// Standard ref, no useStorage to avoid sync issues
const token = ref(localStorage.getItem('token')); 

const isAdmin = ref(false);
const diningInfo = ref(null);
const debugMsg = ref('-');

const checkRole = () => {
  // Always read fresh from localStorage
  const rawToken = localStorage.getItem('token');
  console.log('App: checkRole called. Token in Storage:', rawToken);
  
  token.value = rawToken; // Update reactive state
  
  if (!rawToken) {
    isAdmin.value = false;
    diningInfo.value = null;
    debugMsg.value = 'No Token';
    return;
  }
  
  try {
    const payload = JSON.parse(atob(rawToken.split('.')[1]));
    console.log('App: Token Payload:', payload);
    
    isAdmin.value = payload.roles && payload.roles.includes('ROLE_ADMIN');
    
    // Parse Dining Info for Guests
    if (!isAdmin.value) {
        let mode = payload.diningMode;
        let table = payload.tableNumber;
        
        let source = 'Token';
        
        if (!mode) {
           try {
               const localInfo = JSON.parse(localStorage.getItem('diningInfo') || '{}');
               console.log('App: Local Dining Info:', localInfo);
               if (localInfo.mode) {
                   mode = localInfo.mode;
                   table = localInfo.table;
                   source = 'LocalStorage';
               }
           } catch(e) { console.error('Error reading diningInfo', e); }
        }

        diningInfo.value = {
            mode: mode || 'UNKNOWN', 
            table: table
        };
        debugMsg.value = `Src:${source} Mode:${diningInfo.value.mode}`;
    } else {
        diningInfo.value = null;
        debugMsg.value = 'Admin';
    }
  } catch (e) {
    console.error('Error parsing token:', e);
    isAdmin.value = false;
    diningInfo.value = null;
    debugMsg.value = 'ParseError';
  }
};

// Listen for custom auth-change event from Login.vue
onMounted(() => {
  checkRole();
  window.addEventListener('auth-change', checkRole);
});

// Watch route as backup
watch(() => route.path, () => {
    checkRole();
});

const isMenuOpen = ref(false);

const handleLogout = () => {
  const wasAdmin = isAdmin.value;
  
  // Use reactive token to trigger UI updates immediately
  token.value = null; 
  // Just in case, clear cart too
  localStorage.removeItem('cart');
  localStorage.removeItem('diningInfo');
  localStorage.removeItem('token'); // Ensure it's gone from storage
  localStorage.removeItem('roles');
  
  window.dispatchEvent(new Event('auth-change'));
  
  if (wasAdmin) {
    router.push('/staff/login');
  } else {
    // For Guests, go back to Menu page to see the Welcome Dialog again
    router.push('/menu');
    // Force reload to reset all states cleanly if needed, though router.push usually enough
    // setTimeout(() => window.location.reload(), 50); 
  }
};
</script>

<template>
  <div class="bg-gray-100 min-h-screen">
    <nav v-if="!$route.meta.hideNavbar" class="bg-gradient-to-r from-orange-500 to-red-600 shadow-lg p-4 mb-4 sticky top-0 z-[500] w-full">
      <div class="max-w-7xl mx-auto flex justify-between items-center">
        <div class="flex items-center gap-3">
          <!-- Logo Icon -->
          <div class="bg-white/20 p-2 rounded-lg backdrop-blur-sm">
             <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
          </div>
          <div class="font-extrabold text-2xl text-white tracking-tight">Restaurant App</div>
          <!-- Debug Info (Temporary) -->
          <div class="text-xs text-yellow-200 font-mono ml-4 bg-black/20 p-1 rounded">
            DB: {{ debugMsg }}
          </div>
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
              
              <!-- Guest Status & Switch Mode -->
              <!-- Admin Logout / Guest Sign In -->
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
          
          <router-link v-if="!isAdmin" to="/my-orders" class="text-white hover:bg-white/10 px-4 py-3 rounded-md font-medium" active-class="bg-white/20" @click="isMenuOpen = false">My Orders</router-link>
          
          <router-link v-if="isAdmin" to="/admin" class="text-white hover:bg-white/10 px-4 py-3 rounded-md font-medium flex items-center gap-2" active-class="bg-white/20" @click="isMenuOpen = false">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg>
              Admin Dashboard
          </router-link>
          
          <div class="h-px bg-white/20 mx-2 my-2"></div>
          
          <!-- Mobile Guest Status -->
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
    <ErrorBoundary>
        <router-view :key="$route.fullPath"></router-view>
    </ErrorBoundary>
  </div>
</template>
