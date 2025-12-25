<script setup>
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { encrypt } from '../utils/encryption';
import request from '../utils/request';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

const router = useRouter();
const route = useRoute();

const username = ref('');
const password = ref('');
const errorMsg = ref('');
const isLoading = ref(false);

const handleResponse = (res) => {
    if (res.token) {
        localStorage.setItem('token', res.token);
        
        // Store roles if available
        if (res.roles) {
            localStorage.setItem('roles', JSON.stringify(res.roles));
            
            // RBAC Redirect logic
            const redirectPath = route.query.redirect;
            
            if (redirectPath && redirectPath !== '/staff/login') {
                 router.push(redirectPath);
            } else {
                // Default staff landing
                router.push('/admin');
            }
        } else {
            // Fallback (though staff should have roles)
            localStorage.setItem('roles', '[]');
            router.push('/admin');
        }
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
         <CardHeader class="space-y-1 pb-6">
             <CardTitle class="text-2xl font-bold text-center">Staff Login</CardTitle>
             <CardDescription class="text-center">
                 Authorized personnel only
             </CardDescription>
         </CardHeader>

         <CardContent class="grid gap-4">
             <div class="space-y-4">
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
                 <Button class="w-full bg-primary hover:bg-primary/90" @click="handleLogin" :disabled="isLoading">
                    {{ isLoading ? 'Logging in...' : 'Login' }}
                 </Button>
             </div>
             
             <p v-if="errorMsg" class="text-destructive text-sm font-medium text-center bg-destructive/10 p-2 rounded">{{ errorMsg }}</p>
         </CardContent>
     </Card>
  </div>
</template>
