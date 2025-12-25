<script setup>
import { ref } from 'vue'
import { onBeforeRouteLeave } from 'vue-router';
import MenuList from './MenuList.vue'
import Cart from './Cart.vue'
import DiningModeDialog from './DiningModeDialog.vue';
import { Button, Sheet, SheetContent, SheetTrigger, Toast } from '@mini-restaurant/ui'
import { useCartStore } from '../stores/cart'
import { storeToRefs } from 'pinia'

const cartStore = useCartStore();
const { totalQuantity } = storeToRefs(cartStore);

const showToast = ref(false);
const toastMessage = ref('');
const isSheetOpen = ref(false);

const handleAddToCart = (itemName) => {
    toastMessage.value = `Added "${itemName}" to cart`;
    showToast.value = true;
    setTimeout(() => {
        showToast.value = false;
    }, 3000);
};

// Zombie Fix: Force unmount
const isActive = ref(true);
onBeforeRouteLeave((to, from, next) => {
    console.log("MenuPage: Force Destroying DOM before route leave (Hard Reload Strategy)");
    
    // Force a hard reload to ensure all memory/DOM is cleared and no framework crashes occur
    // This is the most robust solution for the Federation Zombie/Crash issue
    window.location.href = to.fullPath;
    
    // Do not call next() because we are reloading
    // prevents Vue Router from trying to handle the transition
});
</script>

<template>
  <div v-if="isActive" id="sub-app-menu" class="min-h-screen bg-gray-50/50 font-sans">
    <Toast :show="showToast" title="Success" :message="toastMessage" type="success" @close="showToast = false" />
    <DiningModeDialog />
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="flex flex-col lg:flex-row gap-8 relative">
             <!-- Menu Section (70%) -->
             <main class="w-full lg:w-[70%]">
                 <div class="flex items-center justify-between mb-8">
                     <div>
                        <h1 class="text-4xl font-extrabold tracking-tight text-foreground lg:text-5xl mb-2">
                            Menu
                        </h1>
                        <p class="text-muted-foreground text-lg">
                            Select your favorite dishes.
                        </p>
                     </div>
                 </div>
                 <MenuList @added-to-cart="handleAddToCart" />
             </main>

             <!-- Cart Section (30% - Sticky on Desktop) -->
             <aside class="hidden lg:block w-full lg:w-[30%]">
                 <div class="sticky top-24 h-[calc(100vh-8rem)]">
                     <Cart />
                 </div>
             </aside>
        </div>
    </div>

    <!-- Mobile Floating Cart Button -->
    <div class="lg:hidden fixed bottom-8 right-6 z-[100] pb-[env(safe-area-inset-bottom)]">
        <div class="relative">
             <!-- Manual Trigger Button (Outside Sheet) -->
            <button 
                @click="isSheetOpen = true"
                class="flex items-center justify-center h-16 w-16 rounded-full shadow-2xl bg-gray-900 text-white hover:bg-black transition-all hover:scale-105 active:scale-95 cursor-pointer z-[9999]"
            >
                 <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="21" r="1"/><circle cx="19" cy="21" r="1"/><path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"/></svg>
            </button>
            <span v-if="totalQuantity > 0" class="absolute -top-1 -right-1 flex h-6 w-6 items-center justify-center rounded-full bg-red-600 text-[11px] font-bold text-white shadow-md border-2 border-white pointer-events-none z-[10000]">
                {{ totalQuantity }}
            </span>

            <Sheet v-model:open="isSheetOpen">
                <SheetContent side="bottom" class="h-[85vh] rounded-t-[20px] p-0 border-0 shadow-2xl z-[10000]">
                    <div class="h-full pt-6">
                         <Cart />
                    </div>
                </SheetContent>
            </Sheet>
        </div>
    </div>
  </div>
</template>

