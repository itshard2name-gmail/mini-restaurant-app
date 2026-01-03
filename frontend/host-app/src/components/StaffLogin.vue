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
        localStorage.setItem('admin_token', res.token);
        
        // Store roles if available
        if (res.roles) {
            localStorage.setItem('admin_roles', JSON.stringify(res.roles));
            
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
            localStorage.setItem('admin_roles', '[]');
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
  <div class="h-screen w-full flex bg-gray-50 dark:bg-gray-950 text-gray-900 dark:text-white overflow-hidden">
     
     <!-- Left Side: Branding & Visuals (Desktop Only) -->
     <div class="hidden lg:flex w-1/2 relative flex-col justify-between p-12 border-r border-gray-200 dark:border-gray-800 bg-gray-900 text-white">
        <!-- Abstract Background -->
        <div class="absolute inset-0 z-0 opacity-30 overflow-hidden">
            <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-purple-600 rounded-full mix-blend-screen filter blur-[100px] animate-blob"></div>
            <div class="absolute bottom-1/4 right-1/4 w-96 h-96 bg-indigo-600 rounded-full mix-blend-screen filter blur-[100px] animate-blob animation-delay-2000"></div>
        </div>

        <!-- content -->
        <div class="relative z-10">
           <div class="flex items-center gap-3">
              <div class="bg-indigo-600 p-2 rounded-lg">
                  <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
              </div>
              <span class="text-xl font-bold tracking-tight">Mini Restaurant</span>
           </div>
        </div>

        <div class="relative z-10 max-w-lg">
            <h1 class="text-5xl font-extrabold tracking-tight mb-6 leading-tight">
               Manage your restaurant <br/>
               <span class="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 to-purple-400">like a pro.</span>
            </h1>
            <p class="text-lg text-gray-400 leading-relaxed">
               Real-time order tracking, menu management, and business analytics all in one secure platform.
            </p>
        </div>

        <div class="relative z-10 flex items-center gap-4 text-sm text-gray-500">
           <span>&copy; 2025 Mini Restaurant Corp.</span>
           <span>•</span>
           <span>Version 2.0</span>
        </div>
     </div>

     <!-- Right Side: Login Form -->
     <div class="w-full lg:w-1/2 flex items-center justify-center p-8 relative">
        <!-- Mobile Background (Subtle) -->
         <div class="absolute inset-0 z-0 lg:hidden opacity-20">
            <div class="absolute top-0 right-0 w-64 h-64 bg-indigo-900 rounded-full filter blur-3xl"></div>
         </div>

         <div class="w-full max-w-md space-y-8 relative z-10">
             <div class="text-center lg:text-left">
                 <h2 class="text-3xl font-bold tracking-tight text-gray-900 dark:text-white">Welcome back</h2>
                 <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">Please sign in to your admin account</p>
             </div>

             <div class="grid gap-6">
                 <div class="space-y-4">
                     <div class="grid gap-2">
                         <Label htmlFor="username" class="text-gray-700 dark:text-gray-300">Username</Label>
                         <div class="relative">
                             <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <span class="text-gray-500">
                                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                                </span>
                             </div>
                             <Input 
                                id="username" 
                                v-model="username" 
                                type="text" 
                                placeholder="admin"
                                class="pl-10 h-11 bg-white dark:bg-gray-900/50 border-gray-300 dark:border-gray-700 text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-600 focus:ring-indigo-500 focus:border-indigo-500 transition-colors"
                             />
                         </div>
                     </div>
                     <div class="grid gap-2">
                         <div class="flex items-center justify-between">
                            <Label htmlFor="password" class="text-gray-700 dark:text-gray-300">Password</Label>
                            <!-- Hypothetical Link -->
                            <a href="#" class="text-xs text-indigo-600 dark:text-indigo-400 hover:text-indigo-500 dark:hover:text-indigo-300">Forgot password?</a>
                         </div>
                         <div class="relative">
                             <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <span class="text-gray-500">
                                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                                </span>
                             </div>
                             <Input 
                                id="password" 
                                v-model="password" 
                                type="password" 
                                placeholder="••••••••"
                                @keyup.enter="handleLogin"
                                class="pl-10 h-11 bg-white dark:bg-gray-900/50 border-gray-300 dark:border-gray-700 text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-600 focus:ring-indigo-500 focus:border-indigo-500 transition-colors"
                             />
                         </div>
                     </div>
                     
                     <div v-if="errorMsg" class="flex items-center gap-2 text-red-600 dark:text-red-400 text-sm bg-red-50 dark:bg-red-950/50 p-3 rounded border border-red-200 dark:border-red-900/50">
                        <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                        <span>{{ errorMsg }}</span>
                     </div>

                     <Button class="w-full h-11 bg-indigo-600 hover:bg-indigo-500 text-white font-medium rounded-md shadow-lg shadow-indigo-500/20 transition-all text-base" @click="handleLogin" :disabled="isLoading">
                        <span v-if="isLoading" class="flex items-center gap-2">
                            <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                            Authenticating...
                        </span>
                        <span v-else>Sign In</span>
                     </Button>
                 </div>
                 
                 <div class="relative">
                    <div class="absolute inset-0 flex items-center">
                      <span class="w-full border-t border-gray-300 dark:border-gray-800" />
                    </div>
                    <div class="relative flex justify-center text-xs uppercase">
                      <span class="bg-gray-50 dark:bg-gray-950 px-2 text-gray-500">Or continue to</span>
                    </div>
                 </div>

                 <div class="text-center">
                     <router-link to="/menu" class="inline-flex items-center justify-center gap-2 text-sm font-medium text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors py-2">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path></svg>
                        Customer Storefront
                     </router-link>
                 </div>
             </div>
         </div>
     </div>
  </div>
</template>

<style scoped>
.animate-blob {
  animation: blob 10s infinite;
}
.animation-delay-2000 {
  animation-delay: 2s;
}
@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(50px, -50px) scale(1.2); }
  66% { transform: translate(-30px, 30px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}
</style>
