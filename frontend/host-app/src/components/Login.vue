<script setup>
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '../utils/request';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

const router = useRouter();
const route = useRoute();

const phone = ref('');
const diningMode = ref('DINE_IN'); // 'DINE_IN' or 'TAKEOUT'
const tableNumber = ref('');
const errorMsg = ref('');
const isLoading = ref(false);

// Fixed Mode logic for Checkout Redirect
const isFixedMode = ref(false);

import { onMounted } from 'vue';
onMounted(() => {
    if (route.query.mode && ['DINE_IN', 'TAKEOUT'].includes(route.query.mode)) {
        diningMode.value = route.query.mode;
        isFixedMode.value = true;
    } else {
        // Fallback: Check LocalStorage if mode is already set (Context Awareness)
        try {
            const savedInfo = JSON.parse(localStorage.getItem('diningInfo') || '{}');
            if (savedInfo.mode === 'TAKEOUT') {
                diningMode.value = 'TAKEOUT';
                // If user is already in Takeout context, we treat it as fixed mode to avoid confusion
                isFixedMode.value = true;
            }
        } catch (e) {}
    }
});

const handleResponse = async (res) => {
    if (res.token) {
        localStorage.setItem('token', res.token);
        
        // Store Dining Info in LocalStorage (Frontend Persistence)
        if (diningMode.value) {
            localStorage.setItem('diningInfo', JSON.stringify({
                mode: diningMode.value,
                table: tableNumber.value
            }));
        }
        
        // Notify App.vue to update state immediately
        window.dispatchEvent(new Event('auth-change'));
        
        // --- Merge Guest Orders Logic ---
        const guestToken = localStorage.getItem('guest_order_token');
        if (guestToken) {
            try {
                console.log('Found Guest Token, merging orders...');
                await request.post('/orders/merge', null, {
                    params: { guestToken: guestToken }
                });
                // Remove guest tokens to prevent re-merge or confusion
                localStorage.removeItem('guest_order_token');
                localStorage.removeItem('guest_order_id');
                console.log('Orders merged successfully');
            } catch (err) {
                console.warn('Failed to merge guest orders', err);
            }
        }
        // --------------------------------
        
        // Store roles if available
        if (res.roles) {
            localStorage.setItem('roles', JSON.stringify(res.roles));
            
            // RBAC Redirect logic
            const redirectPath = route.query.redirect;
            
            if (redirectPath && redirectPath !== '/login') {
                 router.push(redirectPath);
            } else {
                router.push('/menu');
            }
        } else {
            // Fallback
            localStorage.setItem('roles', '[]');
            router.push('/menu');
        }
    }
};

const handleQuickLogin = async () => {
    if (!phone.value) {
        errorMsg.value = "Please enter your mobile number";
        return;
    }
    
    if (diningMode.value === 'DINE_IN' && !tableNumber.value) {
        errorMsg.value = "Please enter your table number";
        return;
    }
    
    isLoading.value = true;
    errorMsg.value = '';

    try {
        const res = await request.post('/auth/quick-login', {
            phone: phone.value,
            diningMode: diningMode.value,
            tableNumber: diningMode.value === 'DINE_IN' ? tableNumber.value : null
        });
        handleResponse(res);
    } catch (e) {
        errorMsg.value = "Quick Login failed. Please try again.";
        console.error(e);
    } finally {
        isLoading.value = false;
    }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-50/50">
     <div class="absolute inset-0 bg-gradient-to-r from-orange-500/10 to-red-600/10 z-0"></div>
     
     <Card class="w-full max-w-md z-10 shadow-xl border-t-4 border-t-primary">
         <CardHeader class="space-y-1 pb-6">
             <CardTitle class="text-2xl font-bold text-center">Welcome</CardTitle>
             <CardDescription class="text-center">
                 Sign in to continue your order
             </CardDescription>
         </CardHeader>
         
         <CardContent class="grid gap-4">
             <!-- Customer Login Form -->
             <div class="space-y-4">
                 <!-- Dining Mode Selection -->
                 <div v-if="!isFixedMode" class="grid grid-cols-2 gap-2 mb-4">
                    <button 
                        @click="diningMode = 'DINE_IN'"
                        class="p-2 rounded-md font-medium text-sm transition-colors border-2"
                        :class="diningMode === 'DINE_IN' ? 'border-primary bg-primary/10 text-primary' : 'border-transparent bg-gray-100 text-gray-600 hover:bg-gray-200'"
                    >
                        Accomo (Dine-in)
                    </button>
                    <button 
                        @click="diningMode = 'TAKEOUT'"
                        class="p-2 rounded-md font-medium text-sm transition-colors border-2"
                        :class="diningMode === 'TAKEOUT' ? 'border-primary bg-primary/10 text-primary' : 'border-transparent bg-gray-100 text-gray-600 hover:bg-gray-200'"
                    >
                        Takeout
                    </button>
                 </div>

                 <div v-if="diningMode === 'DINE_IN'" class="grid gap-2">
                     <Label htmlFor="tableNumber">Table Number</Label>
                     <Input 
                        id="tableNumber" 
                        v-model="tableNumber" 
                        type="number" 
                        placeholder="e.g. 5" 
                     />
                 </div>

                 <div class="grid gap-2">
                     <Label htmlFor="phone">Mobile Number</Label>
                     <Input 
                        id="phone" 
                        v-model="phone" 
                        type="tel" 
                        placeholder="0912345678" 
                        @keyup.enter="handleQuickLogin"
                     />
                 </div>
                 <Button class="w-full bg-primary hover:bg-primary/90" @click="handleQuickLogin" :disabled="isLoading">
                    {{ isLoading ? 'Processing...' : 'Start Ordering' }}
                 </Button>
             </div>
             
             <p v-if="errorMsg" class="text-destructive text-sm font-medium text-center bg-destructive/10 p-2 rounded">{{ errorMsg }}</p>
         </CardContent>
     </Card>
  </div>
</template>
