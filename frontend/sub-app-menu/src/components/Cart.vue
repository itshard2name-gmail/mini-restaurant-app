<script setup>
import { ref } from 'vue';
import { useCartStore } from '../stores/cart';
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ScrollArea, Separator, Button, Card, CardHeader, CardTitle, CardContent, CardFooter, Badge } from '@mini-restaurant/ui';

const cartStore = useCartStore();
    const { cartItems, totalPrice, totalQuantity, orderType, tableNumber } = storeToRefs(cartStore);
    const { addToCart, minusItem, removeItem, clearCart } = cartStore;
    const router = useRouter();
    
    const loading = ref(false);
    const successMessage = ref('');
    const errorMessage = ref('');
    
    const submitOrder = async () => {
        console.log("DEBUG: Submit Order Clicked");
        console.log("DEBUG: Order Type:", orderType.value);
        console.log("DEBUG: Table Number:", tableNumber.value);
        
        loading.value = true;
        successMessage.value = '';
        errorMessage.value = '';
    
        const token = localStorage.getItem('token');
        console.log("DEBUG: Token exists?", !!token);
        
        // Validation: Dining Mode
        if (!orderType.value) {
            console.log("DEBUG: No Order Type");
            errorMessage.value = "Please select Dining Mode (Refresh page if dialog missing)";
            loading.value = false;
            return;
        }

        // Check Token Type (Real User vs Table User)
        let isTableUser = false;
        if (token) {
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                const sub = payload.sub || '';
                console.log("DEBUG: Token Sub:", sub);
                isTableUser = sub.startsWith('Table-');
                console.log("DEBUG: Is Table User?", isTableUser);
            } catch(e) {
                console.error("DEBUG: Token Parse Error", e);
            }
        }

        if (!token || (orderType.value === 'TAKEOUT' && isTableUser)) {
            console.log("DEBUG: Entering Redirect Logic...");
            // Rule: Takeout requires a Real User (Phone Number) for "Total Account" tracking.
            // If No Token OR If currently logged in as a "Table User" (Anonymous Dine-In),
            // we must force a real login.
            
            if (orderType.value !== 'DINE_IN') {
                 console.log("DEBUG: Redirecting to Login for Takeout...");
                 loading.value = false;
                 // Use window.location.href to navigate to Host App route
                 window.location.href = '/login?redirect=/menu&mode=TAKEOUT';
                 return;
            }
            console.log("DEBUG: Proceeding as Guest for DINE_IN");
            // For DINE_IN without token (Guest), we proceed. 
            // (Note: Table User has token, so they skip this block for DINE_IN, which is correct).
        }

        let userId = 'admin';
        if (token) {
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                userId = payload.sub || 'admin';
            } catch (e) { }
        } else {
            userId = null; // Guest
        }
    
        const orderItems = cartItems.value.map(item => ({
            menuId: item.menuId,
            quantity: item.quantity,
            notes: item.notes
        }));
    
        if (orderItems.length === 0) {
            errorMessage.value = "Cart is empty.";
            loading.value = false;
            return;
        }
    
        try {
            const headers = {};
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
                headers['X-User-Id'] = userId;
            }

            // --- MERGE LOGIC START ---
            let targetOrderId = null;
            
            // SHADOW GUEST TOKEN: Always retrieve or persist
            let guestToken = localStorage.getItem('guest_order_token');
            // If we have a token but no guestToken yet, we should probably generate one or let backend do it.
            // But to ensure session continuity, we rely on backend returning it if we don't have one.
            // If we DO have one, we MUST send it.

            // 1. Guest Merge Check
            if (!token && orderType.value === 'DINE_IN') {
                const storedId = localStorage.getItem('guest_order_id');
                if (storedId && guestToken) {
                    targetOrderId = storedId;
                }
            }
            // 2. User Merge Check (Takeout or Dine-In)
            else if (token) {
                try {
                    const activeRes = await axios.get('/api/orders/my/active', { headers });
                    if (activeRes.data && activeRes.data.length > 0) {
                        const activeOrder = activeRes.data[0];
                        if (activeOrder.orderType === orderType.value) {
                             if (orderType.value === 'DINE_IN' && activeOrder.tableNumber !== tableNumber.value) {
                                 console.log("Skipping merge: Table number mismatch");
                             } else {
                                 targetOrderId = activeOrder.id;
                             }
                        } else {
                            console.log(`Skipping merge: Order Type mismatch`);
                        }
                    }
                } catch (checkErr) {
                    console.warn("Failed to check active orders for merge:", checkErr);
                }
            }

            let response;
            if (targetOrderId) {
                try {
                    console.log("Attempting to merge into Order ID:", targetOrderId);
                    // For Add Items, we pass token/guestToken via query/headers if needed, 
                    // but usually userId/guestToken on the Order itself is the check.
                    // Shadow Token: We might want to ensure the order has the token, but addItems doesn't update Order level fields usually.
                    const url = `/api/orders/${targetOrderId}/items` + (guestToken ? `?token=${guestToken}` : '');
                    response = await axios.post(url, orderItems, { headers });
                } catch (mergeErr) {
                    console.warn("Merge failed, falling back to new order:", mergeErr);
                    targetOrderId = null;
                }
            }

            if (!targetOrderId) {
                // Create New Order with Shadow Token
                response = await axios.post('/api/orders', { 
                    items: orderItems,
                    orderType: orderType.value,
                    tableNumber: tableNumber.value,
                    guestToken: guestToken // <--- Send Shadow Token
                }, { headers });
            }
            // --- MERGE LOGIC END ---

            // Handle Guest Token (Update if returned)
            if (response.data.guestToken) {
                localStorage.setItem('guest_order_id', response.data.id);
                localStorage.setItem('guest_order_token', response.data.guestToken);
            }

            successMessage.value = 'Order submitted successfully!';
            clearCart();
            window.scrollTo({ top: 0, behavior: 'smooth' });
            
            // Auto-redirect to My Orders
            setTimeout(() => {
                 router.push('/my-orders');
            }, 500); 
            
        } catch (error) {
            console.error('Order failed:', error);
            // Handle specific backend errors
            if (error.response && error.response.data && error.response.data.message) {
                 errorMessage.value = 'Failed: ' + error.response.data.message;
            } else {
                 errorMessage.value = 'Failed to submit order.';
            }
        } finally {
            loading.value = false;
        }
    };
</script>

<template>
    <Card class="h-full flex flex-col border-0 shadow-none">
        <CardHeader class="pb-3">
            <CardTitle class="flex justify-between items-center text-xl">
                <span>Shopping Cart</span>
                
                <div class="flex items-center gap-2">
                    <!-- Dining Mode Badge -->
                    <Badge v-if="orderType === 'DINE_IN'" variant="secondary" class="bg-indigo-50 text-indigo-700 border-indigo-100 flex items-center gap-1.5 px-2.5 py-1">
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2v0a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/></svg>
                          <span class="font-bold text-xs">Table {{ tableNumber || '?' }}</span>
                    </Badge>
                     <Badge v-if="orderType === 'TAKEOUT'" variant="secondary" class="bg-emerald-50 text-emerald-700 border-emerald-100 flex items-center gap-1.5 px-2.5 py-1">
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="18" x2="23" y1="8" y2="13"/><line x1="23" x2="18" y1="13" y2="13"/></svg>
                          <span class="font-bold text-xs">Takeout</span>
                    </Badge>

                    <Badge variant="secondary" class="bg-primary/10 text-primary hover:bg-primary/20">
                        {{ totalQuantity }} items
                    </Badge>
                </div>
            </CardTitle>
        </CardHeader>
        
        <CardContent class="flex-grow overflow-hidden p-0">
            <div v-if="successMessage" class="mx-6 mb-4 p-3 bg-green-50 text-green-700 rounded text-sm">
                {{ successMessage }}
            </div>
            <div v-if="errorMessage" class="mx-6 mb-4 p-3 bg-red-50 text-red-700 rounded text-sm">
                {{ errorMessage }}
            </div>

            <ScrollArea class="h-full px-6">
                <div v-if="cartItems.length === 0" class="text-muted-foreground text-center py-10">
                    Your cart is empty.
                </div>
                <div v-else class="space-y-4">
                    <div v-for="(item, index) in cartItems" :key="item.menuId">
                        <div class="flex flex-col gap-2 py-3">
                            <div class="flex justify-between items-start">
                                <div>
                                    <p class="font-medium leading-none">{{ item.name }}</p>
                                    <p class="text-xs text-muted-foreground mt-1">Unit: ${{ item.price }}</p>
                                </div>
                                <div class="font-semibold">
                                    ${{ (item.price * item.quantity).toFixed(2) }}
                                </div>
                            </div>
                            
                            <div class="flex justify-between items-center mt-2">
                                <div class="flex items-center gap-3 bg-secondary/20 rounded-lg p-1">
                                    <button 
                                        @click="minusItem({ id: item.menuId })"
                                        class="w-6 h-6 flex items-center justify-center rounded-md hover:bg-white shadow-sm transition-all text-sm font-bold disabled:opacity-50"
                                    >
                                        −
                                    </button>
                                    <span class="text-sm font-medium w-4 text-center">{{ item.quantity }}</span>
                                    <button 
                                        @click="addToCart({ id: item.menuId })"
                                        class="w-6 h-6 flex items-center justify-center rounded-md hover:bg-white shadow-sm transition-all text-sm font-bold"
                                    >
                                        +
                                    </button>
                                </div>
                                <button 
                                    @click="removeItem({ id: item.menuId })"
                                    class="text-xs text-red-500 hover:text-red-700 hover:underline px-2"
                                >
                                    Remove
                                </button>
                            </div>

                            <!-- Notes Input -->
                            <div class="mt-2">
                                <textarea 
                                    v-model="item.notes" 
                                    placeholder="Add notes (e.g. No onions)" 
                                    class="w-full text-xs p-2 rounded-md border border-input bg-background resize-none focus:outline-none focus:ring-1 focus:ring-ring"
                                    rows="2"
                                ></textarea>
                            </div>
                        </div>
                        <Separator v-if="index < cartItems.length - 1" />
                    </div>
                </div>
            </ScrollArea>
        </CardContent>

        <CardFooter class="flex-col gap-4 border-t pt-6">
            <div class="flex justify-between items-center w-full text-lg font-bold">
                <span>Total</span>
                <span class="text-primary">${{ totalPrice.toFixed(2) }}</span>
            </div>
            <Button 
                class="w-full text-lg h-12" 
                :disabled="loading || cartItems.length === 0" 
                @click="submitOrder"
            >
                <span v-if="loading" class="mr-2 animate-spin">⟳</span>
                {{ loading ? 'Processing...' : 'Checkout Now' }}
            </Button>
        </CardFooter>
    </Card>
</template>
