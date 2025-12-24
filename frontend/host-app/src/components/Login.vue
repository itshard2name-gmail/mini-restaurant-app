<script setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { encrypt } from '../utils/encryption';
import request from '../utils/request';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

const router = useRouter();
const route = useRoute();

const activeTab = ref('customer'); // 'customer' or 'staff'
const username = ref('');
const password = ref('');
const phone = ref('');
const token = ref('');
const errorMsg = ref('');

const isLoading = ref(false);

const handleResponse = (res) => {
    if (res.token) {
        token.value = res.token;
        localStorage.setItem('token', res.token);
        
        // Store roles if available
        if (res.roles) {
            localStorage.setItem('roles', JSON.stringify(res.roles));
            
            // RBAC Redirect logic
            const redirectPath = route.query.redirect;
            
            if (redirectPath && redirectPath !== '/login') {
                 router.push(redirectPath);
            } else if (res.roles.includes('ROLE_ADMIN')) {
                router.push('/admin');
            } else {
                router.push('/menu');
            }
        } else {
            // Fallback
            localStorage.setItem('roles', '[]');
            const redirectPath = route.query.redirect;
            router.push(redirectPath || '/menu');
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
        const res = await request.post('/auth/quick-login', {
            phone: phone.value
        });
        handleResponse(res);
    } catch (e) {
        errorMsg.value = "Quick Login failed. Please try again.";
        console.error(e);
    } finally {
        isLoading.value = false;
    }
};

const handleLogin = async () => {
    isLoading.value = true;
    errorMsg.value = '';
    
    try {
        const encryptedPassword = await encrypt(password.value);
        if(!encryptedPassword) {
            errorMsg.value = "Encryption failed";
            return;
        }

        const res = await request.post('/auth/login', {
            username: username.value,
            password: encryptedPassword
        });
        handleResponse(res);
    } catch (e) {
        errorMsg.value = "Login failed: Invalid credentials";
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
         <CardHeader class="space-y-1 pb-2">
             <CardTitle class="text-2xl font-bold text-center">Welcome</CardTitle>
             <CardDescription class="text-center">
                 Sign in to continue your order
             </CardDescription>
         </CardHeader>
         
         <div class="px-6 pb-2">
             <div class="flex p-1 bg-secondary/20 rounded-lg">
                 <button 
                    @click="activeTab = 'customer'"
                    class="flex-1 py-1.5 text-sm font-medium rounded-md transition-all"
                    :class="activeTab === 'customer' ? 'bg-white shadow text-primary' : 'text-muted-foreground hover:text-foreground'"
                 >
                    Customer
                 </button>
                 <button 
                    @click="activeTab = 'staff'"
                    class="flex-1 py-1.5 text-sm font-medium rounded-md transition-all"
                    :class="activeTab === 'staff' ? 'bg-white shadow text-primary' : 'text-muted-foreground hover:text-foreground'"
                 >
                    Staff
                 </button>
             </div>
         </div>

         <CardContent class="grid gap-4 pt-4">
             <!-- Customer Tab -->
             <div v-if="activeTab === 'customer'" class="space-y-4">
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
                 <Button class="w-full" @click="handleQuickLogin" :disabled="isLoading">
                    {{ isLoading ? 'Processing...' : 'Track Order / Quick Login' }}
                 </Button>
             </div>

             <!-- Staff Tab -->
             <div v-if="activeTab === 'staff'" class="space-y-4">
                 <div class="grid gap-2">
                     <Label htmlFor="username">Username</Label>
                     <Input id="username" v-model="username" type="text" placeholder="admin" />
                 </div>
                 <div class="grid gap-2">
                     <Label htmlFor="password">Password</Label>
                     <Input 
                        id="password" 
                        v-model="password" 
                        type="password" 
                        @keyup.enter="handleLogin"
                     />
                 </div>
                 <Button class="w-full" @click="handleLogin" :disabled="isLoading">
                    {{ isLoading ? 'Logging in...' : 'Login' }}
                 </Button>
             </div>
             
             <p v-if="errorMsg" class="text-destructive text-sm font-medium text-center bg-destructive/10 p-2 rounded">{{ errorMsg }}</p>
         </CardContent>
     </Card>
  </div>
</template>
