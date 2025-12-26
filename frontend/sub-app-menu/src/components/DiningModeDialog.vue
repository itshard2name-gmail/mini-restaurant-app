<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useCartStore } from '../stores/cart';
import { Button, Card, CardContent, CardHeader, CardTitle, CardDescription } from '@mini-restaurant/ui';
import axios from 'axios';

const cartStore = useCartStore();
const isOpen = ref(false); // Controls visibility
const step = ref(1); // 1: Mode Select, 2: Table Select (if Dine-in)

const availableTables = ref([]); // Will fetch from Settings
const loadingTables = ref(false);

// Login Logic
const phoneNumber = ref('');
const loadingLogin = ref(false);

const performRealLogin = async () => {
    if (!phoneNumber.value) return;
    loadingLogin.value = true;
    try {
        // Call Real Backend API
        const res = await axios.post('/api/auth/quick-login', {
            phone: phoneNumber.value
        });
        
        if (res.data && res.data.token) {
            const token = res.data.token;
            const mode = cartStore.orderType;
            const table = cartStore.tableNumber;
            
            // Save Valid Token
            localStorage.setItem('token', token);
            // Save Frontend Metadata (Since backend token might not have diningMode yet, or we use separate storage)
            localStorage.setItem('diningInfo', JSON.stringify({ mode, table }));
            
            // Dispatch Event
            window.dispatchEvent(new Event('auth-change'));
            
            isOpen.value = false;
        }
    } catch (e) {
        console.error("Login failed", e);
        alert("Login failed. Please try again.");
    } finally {
        loadingLogin.value = false;
    }
};

const selectMode = (mode) => {
    cartStore.orderType = mode;
    if (mode === 'TAKEOUT') {
        cartStore.tableNumber = null;
        
        // CHECK IF LOGGED IN
        if (localStorage.getItem('token')) {
             // Just update context
             const info = { mode: 'TAKEOUT', table: null };
             localStorage.setItem('diningInfo', JSON.stringify(info));
             // Dispatch event to update Status Bars
             window.dispatchEvent(new Event('auth-change'));
             isOpen.value = false;
             return;
        }

        // Deferred Login: Close dialog immediately. Login happens at Checkout.
        // PERSISTENCE: Save mode to localStorage so Host App (Login.vue) can see it
        localStorage.setItem('diningInfo', JSON.stringify({ mode: 'TAKEOUT', table: null }));
        isOpen.value = false;
    } else {
        step.value = 2; // Go to Table Select
        fetchTables();
    }
};

const fetchTables = async () => {
    loadingTables.value = true;
    try {
        const token = localStorage.getItem('token');
        const headers = token ? { Authorization: `Bearer ${token}` } : {};
        
        // Correct Endpoint: /api/orders/settings (Proxied to Order Service)
        const res = await axios.get('/api/orders/settings', { headers });
        const settings = res.data; 
        
        const tableSetting = settings.find(s => s.settingKey === 'TABLE_LIST');
        
        if (tableSetting && tableSetting.settingValue) {
            availableTables.value = tableSetting.settingValue.split(',').map(t => t.trim());
        } else {
             availableTables.value = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'];
        }

    } catch (e) {
        console.error("Failed to fetch tables", e);
        availableTables.value = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'];
    } finally {
        loadingTables.value = false;
    }
};

const selectTable = (table) => {
    cartStore.tableNumber = table;
    
    // CHECK IF LOGGED IN
    if (localStorage.getItem('token')) {
         // Just update context
         const info = { mode: 'DINE_IN', table: table };
         localStorage.setItem('diningInfo', JSON.stringify(info));
         // Dispatch event to update Status Bars
         window.dispatchEvent(new Event('auth-change'));
         isOpen.value = false;
         return;
    }

    // UX Improvement: For Dine-in, auto-login with Table Number as ID
    // This avoids asking for phone number, making flow smoother
    phoneNumber.value = `Table-${table}`; 
    performRealLogin();
};

const confirmLogin = () => {
    performRealLogin();
};

// Check if we need to show dialog
onMounted(() => {
    if (!cartStore.orderType) {
        isOpen.value = true;
    }
});

// Watch for reset (e.g. after order complete)
watch(() => cartStore.orderType, (newVal) => {
    if (!newVal) {
        isOpen.value = true;
        step.value = 1;
    }
});

import { onBeforeUnmount } from 'vue';
onBeforeUnmount(() => {
    isOpen.value = false;
});
</script>

<template>
    <div v-if="isOpen" class="fixed inset-0 z-[200] bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
            <Card class="w-full max-w-md bg-card shadow-2xl border-0 animate-in fade-in zoom-in duration-300">
                <CardHeader class="text-center pb-2">
                    <CardTitle class="text-2xl font-bold bg-gradient-to-r from-orange-600 to-orange-400 bg-clip-text text-transparent">
                        {{ step === 1 ? 'Welcome!' : 'Select Your Table' }}
                    </CardTitle>
                    <CardDescription class="text-base text-muted-foreground">
                        {{ step === 1 ? 'How would you like to enjoy your meal today?' : 'Please allow us to locate you.' }}
                    </CardDescription>
                </CardHeader>
                <CardContent class="grid gap-6 pt-6">
                    
                    <!-- STEP 1: MODE SELECT -->
                    <div v-if="step === 1" class="grid grid-cols-2 gap-4">
                        <button 
                            @click="selectMode('DINE_IN')"
                            class="flex flex-col items-center justify-center p-6 space-y-3 border-2 border-border rounded-xl hover:border-orange-500 hover:bg-orange-50 transition-all group"
                        >
                            <div class="p-3 bg-orange-100 rounded-full text-orange-600 group-hover:scale-110 transition-transform">
                                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2v0a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/></svg>
                            </div>
                            <span class="font-semibold text-card-foreground">Dine In</span>
                        </button>

                        <button 
                            @click="selectMode('TAKEOUT')"
                            class="flex flex-col items-center justify-center p-6 space-y-3 border-2 border-border rounded-xl hover:border-green-500 hover:bg-green-50 transition-all group"
                        >
                            <div class="p-3 bg-green-100 rounded-full text-green-600 group-hover:scale-110 transition-transform">
                               <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="18" x2="23" y1="8" y2="13"/><line x1="23" x2="18" y1="13" y2="13"/></svg>
                            </div>
                            <span class="font-semibold text-card-foreground">Takeout</span>
                        </button>
                    </div>

                    <!-- STEP 2: TABLE SELECT -->
                    <div v-else-if="step === 2" class="space-y-4">
                         <div class="grid grid-cols-4 gap-2 max-h-60 overflow-y-auto p-1">
                            <button 
                                v-for="tbl in availableTables"
                                :key="tbl"
                                @click="selectTable(tbl)"
                                class="py-3 bg-muted hover:bg-primary hover:text-primary-foreground rounded-lg font-medium transition-colors border border-border"
                            >
                                {{ tbl }}
                            </button>
                         </div>
                         <Button variant="ghost" class="w-full text-muted-foreground" @click="step = 1">
                            Back
                         </Button>
                    </div>



                </CardContent>
            </Card>
        </div>
</template>
