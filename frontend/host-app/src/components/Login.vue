<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '../utils/request';
import { useSettings } from '../composables/useSettings';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

const router = useRouter();
const route = useRoute();
const { settings, fetchSettings } = useSettings();

const phone = ref('');
const errorMsg = ref('');
const isLoading = ref(false);
const diningContext = ref(null); // 'DINE_IN' or 'TAKEOUT'
const tableContext = ref(null);

onMounted(() => {
    fetchSettings();
    // Context Awareness Logic
    // 1. Check if we have an existing Dine-In session (from QR Scan)
    try {
        const savedInfo = JSON.parse(localStorage.getItem('diningInfo') || '{}');
        if (savedInfo.mode === 'DINE_IN' && savedInfo.table) {
            diningContext.value = 'DINE_IN';
            tableContext.value = savedInfo.table;
        } else {
            // 2. Default to TAKEOUT (Remote / Fresh Session)
            diningContext.value = 'TAKEOUT';
        }
    } catch (e) {
        diningContext.value = 'TAKEOUT';
    }
});

const handleResponse = async (res) => {
    if (res.token) {
        localStorage.setItem('token', res.token);
        
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
    
    isLoading.value = true;
    errorMsg.value = '';

    try {
        // Send resolved context
        const res = await request.post('/auth/quick-login', {
            phone: phone.value,
            diningMode: diningContext.value || 'TAKEOUT',
            tableNumber: tableContext.value // May be null if TAKEOUT
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
  <div class="flex items-center justify-center min-h-screen bg-background">
     
     <Card class="w-full max-w-md z-10 shadow-xl border-t-4 border-t-primary bg-card text-card-foreground">
         <CardHeader class="space-y-1 pb-6">
             <div class="text-center mb-2">
                 <span class="text-xs font-semibold uppercase tracking-widest text-primary/80">{{ settings.BRAND_NAME }}</span>
             </div>
             <CardTitle class="text-2xl font-bold text-center text-foreground">Welcome</CardTitle>
             <CardDescription class="text-center text-muted-foreground">
                 Enter your mobile number to track your order
             </CardDescription>
         </CardHeader>
         
         <CardContent class="grid gap-4">
             <!-- Customer Login Form -->
             <div class="space-y-4">
                 <!-- Smart Context: Only show Mobile Input -->

                 <div class="grid gap-2">
                     <Label htmlFor="phone">Mobile Number</Label>
                     <Input 
                        id="phone" 
                        v-model="phone" 
                        type="tel" 
                        inputmode="numeric"
                        placeholder="0912345678" 
                        class="text-lg h-12"
                        @keyup.enter="handleQuickLogin"
                     />
                 </div>
                 <Button class="w-full bg-primary hover:bg-primary/90 h-10 text-base" @click="handleQuickLogin" :disabled="isLoading">
                    {{ isLoading ? 'Processing...' : 'Track My Order' }}
                 </Button>
                 
                 <p class="text-[0.7rem] text-muted-foreground text-center">
                    No password required. We only use this for your order.
                 </p>
             </div>
             
             <p v-if="errorMsg" class="text-destructive text-sm font-medium text-center bg-destructive/10 p-2 rounded">{{ errorMsg }}</p>
         </CardContent>
     </Card>
  </div>
</template>
