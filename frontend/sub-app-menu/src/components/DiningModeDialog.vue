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

// Mode Selection
const selectMode = (mode) => {
    if (mode === 'TAKEOUT') {
        cartStore.orderType = 'TAKEOUT';
        cartStore.tableNumber = null;
        isOpen.value = false;
    } else {
        cartStore.orderType = 'DINE_IN';
        step.value = 2; // Go to Table Select
        fetchTables();
    }
};

const fetchTables = async () => {
    loadingTables.value = true;
    try {
        // Fetch Settings from Backend to get TABLE_LIST
        // Note: Public endpoint or requires Auth? 
        // OrderService settings endpoint is protected by ADMIN role.
        // We might need a public endpoint for this configuration or hardcode for now if auth is issue.
        // Wait, User is generic guest, might have generated token.
        // Let's try fetching, if 403, we fall back to default list.
        
        const token = localStorage.getItem('token');
        const headers = token ? { Authorization: `Bearer ${token}` } : {};
        
        // Correct Endpoint: /api/orders/settings (Proxied to Order Service)
        const res = await axios.get('/api/orders/settings', { headers });
        const settings = res.data; // Array of {settingKey, settingValue}
        
        const tableSetting = settings.find(s => s.settingKey === 'TABLE_LIST');
        
        if (tableSetting && tableSetting.settingValue) {
            // Parse comma-separated string into array
            availableTables.value = tableSetting.settingValue.split(',').map(t => t.trim());
        } else {
             // Fallback if key missing
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
    isOpen.value = false;
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
            <Card class="w-full max-w-md bg-white shadow-2xl border-0 animate-in fade-in zoom-in duration-300">
                <CardHeader class="text-center pb-2">
                    <CardTitle class="text-2xl font-bold bg-gradient-to-r from-orange-600 to-orange-400 bg-clip-text text-transparent">
                        {{ step === 1 ? 'Welcome!' : 'Select Your Table' }}
                    </CardTitle>
                    <CardDescription class="text-base text-gray-500">
                        {{ step === 1 ? 'How would you like to enjoy your meal today?' : 'Please allow us to locate you.' }}
                    </CardDescription>
                </CardHeader>
                <CardContent class="grid gap-6 pt-6">
                    
                    <!-- STEP 1: MODE SELECT -->
                    <div v-if="step === 1" class="grid grid-cols-2 gap-4">
                        <button 
                            @click="selectMode('DINE_IN')"
                            class="flex flex-col items-center justify-center p-6 space-y-3 border-2 border-gray-100 rounded-xl hover:border-orange-500 hover:bg-orange-50 transition-all group"
                        >
                            <div class="p-3 bg-orange-100 rounded-full text-orange-600 group-hover:scale-110 transition-transform">
                                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2v0a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/></svg>
                            </div>
                            <span class="font-semibold text-gray-900">Dine In</span>
                        </button>

                        <button 
                            @click="selectMode('TAKEOUT')"
                            class="flex flex-col items-center justify-center p-6 space-y-3 border-2 border-gray-100 rounded-xl hover:border-green-500 hover:bg-green-50 transition-all group"
                        >
                            <div class="p-3 bg-green-100 rounded-full text-green-600 group-hover:scale-110 transition-transform">
                               <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="18" x2="23" y1="8" y2="13"/><line x1="23" x2="18" y1="13" y2="13"/></svg>
                            </div>
                            <span class="font-semibold text-gray-900">Takeout</span>
                        </button>
                    </div>

                    <!-- STEP 2: TABLE SELECT -->
                    <div v-else class="space-y-4">
                         <div class="grid grid-cols-4 gap-2 max-h-60 overflow-y-auto p-1">
                            <button 
                                v-for="tbl in availableTables"
                                :key="tbl"
                                @click="selectTable(tbl)"
                                class="py-3 bg-gray-50 hover:bg-gray-900 hover:text-white rounded-lg font-medium transition-colors border border-gray-200"
                            >
                                {{ tbl }}
                            </button>
                         </div>
                         <Button variant="ghost" class="w-full text-gray-500" @click="step = 1">
                            Back
                         </Button>
                    </div>

                </CardContent>
            </Card>
        </div>
</template>
