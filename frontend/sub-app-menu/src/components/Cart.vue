<script setup>
import { ref } from 'vue';
import { useCartStore } from '../stores/cart';
import { storeToRefs } from 'pinia';
import axios from 'axios';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

const cartStore = useCartStore();
const { cartItems, totalPrice, totalQuantity } = storeToRefs(cartStore);
const { addToCart, minusItem, removeItem, clearCart } = cartStore;

const loading = ref(false);
const successMessage = ref('');
const errorMessage = ref('');

const submitOrder = async () => {
    loading.value = true;
    successMessage.value = '';
    errorMessage.value = '';

    const token = localStorage.getItem('token');
    let userId = 'admin';
    if (token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            userId = payload.sub || 'admin';
        } catch (e) { }
    }

    const orderItems = cartItems.value.map(item => ({
        menuId: item.menuId,
        quantity: item.quantity
    }));

    if (orderItems.length === 0) {
        errorMessage.value = "Cart is empty.";
        loading.value = false;
        return;
    }

    try {
        await axios.post('/api/orders', { items: orderItems }, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'X-User-Id': userId
            }
        });
        successMessage.value = 'Order submitted successfully!';
        clearCart();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (error) {
        console.error('Order failed:', error);
        errorMessage.value = 'Failed to submit order.';
    } finally {
        loading.value = false;
    }
};
</script>

<template>
    <Card class="h-full flex flex-col border-0 shadow-none">
        <CardHeader class="pb-3">
            <CardTitle class="flex justify-between items-center text-xl">
                Shopping Cart
                <Badge variant="secondary" class="bg-primary/10 text-primary hover:bg-primary/20">
                    {{ totalQuantity }} items
                </Badge>
            </CardTitle>
        </CardHeader>
        
        <CardContent class="flex-grow overflow-hidden p-0">
            <div v-if="successMessage" class="mx-6 mb-4 p-3 bg-green-50 text-green-700 rounded text-sm">
                {{ successMessage }}
            </div>
            <div v-if="errorMessage" class="mx-6 mb-4 p-3 bg-red-50 text-red-700 rounded text-sm">
                {{ errorMessage }}
            </div>

            <ScrollArea class="h-[calc(100vh-250px)] px-6">
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
                {{ loading ? 'Processing...' : 'Checkout' }}
            </Button>
        </CardFooter>
    </Card>
</template>
