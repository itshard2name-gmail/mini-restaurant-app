<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { encrypt } from '../utils/encryption';
import request from '../utils/request';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

const router = useRouter();
const username = ref('');
const password = ref('');
const token = ref('');
const errorMsg = ref('');

const handleLogin = async () => {
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

        if (res.token) {
            token.value = res.token;
            localStorage.setItem('token', res.token);
            
            // Store roles if available
            if (res.roles) {
                localStorage.setItem('roles', JSON.stringify(res.roles));
                
                // RBAC Redirect
                if (res.roles.includes('ROLE_ADMIN')) {
                    router.push('/admin');
                } else {
                    router.push('/menu');
                }
            } else {
                // Fallback if no roles returned
                localStorage.setItem('roles', '[]');
                router.push('/menu');
            }
        }
    } catch (e) {
        errorMsg.value = "Login failed: Invalid credentials";
        console.error(e);
    }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-50/50">
     <div class="absolute inset-0 bg-gradient-to-r from-orange-500/10 to-red-600/10 z-0"></div> <!-- Subtle background -->
     
     <Card class="w-full max-w-md z-10 shadow-xl border-t-4 border-t-primary">
         <CardHeader class="space-y-1">
             <CardTitle class="text-2xl font-bold text-center">System Login</CardTitle>
             <CardDescription class="text-center">
                 Enter your credentials to access the system
             </CardDescription>
         </CardHeader>
         <CardContent class="grid gap-4">
             <div class="grid gap-2">
                 <Label htmlFor="username">Username</Label>
                 <Input id="username" v-model="username" type="text" placeholder="admin" />
             </div>
             <div class="grid gap-2">
                 <Label htmlFor="password">Password</Label>
                 <Input id="password" v-model="password" type="password" />
             </div>
             
             <p v-if="errorMsg" class="text-destructive text-sm font-medium text-center">{{ errorMsg }}</p>
         </CardContent>
         <CardFooter>
             <Button class="w-full" @click="handleLogin">Login</Button>
         </CardFooter>
         
         <div v-if="token" class="px-6 pb-4 text-xs text-muted-foreground break-all text-center">
              Token generated (check localStorage)
         </div>
     </Card>
  </div>
</template>
