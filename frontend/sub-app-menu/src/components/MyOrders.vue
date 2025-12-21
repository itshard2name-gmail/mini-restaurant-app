<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';

const orders = ref([]);
const loading = ref(false);
const error = ref('');

const fetchMyOrders = async () => {
    loading.value = true;
    error.value = '';
    const token = localStorage.getItem('token');
    let userId = 'admin';
    if (token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            userId = payload.sub || 'admin';
        } catch(e) {}
    }

    try {
        const response = await axios.get('/api/orders/my', {
             headers: {
                'Authorization': `Bearer ${token}`,
                'X-User-Id': userId
             }
        });
        orders.value = response.data;
    } catch (e) {
        console.error('Failed to fetch orders', e);
        error.value = 'Failed to load your orders.';
    } finally {
        loading.value = false;
    }
};

const payOrder = async (orderId) => {
    const token = localStorage.getItem('token');
    try {
        await axios.patch(`/api/orders/${orderId}/pay`, {}, {
             headers: {
                'Authorization': `Bearer ${token}`
             }
        });
        // Refresh orders
        fetchMyOrders();
    } catch (e) {
        console.error('Payment failed', e);
        alert('Payment failed');
    }
};

const getStatusVariant = (status) => {
    switch (status) {
        case 'PAID':
        case 'COMPLETED':
            return 'default'; // Primary color (usually black or dark) - we might want to override for green
        case 'PENDING':
            return 'secondary';
        case 'CANCELLED':
            return 'destructive';
        default:
            return 'outline';
    }
}

const getStatusColorClass = (status) => {
     switch (status) {
        case 'PAID':
        case 'COMPLETED':
            return 'bg-emerald-500 hover:bg-emerald-600 border-transparent'; 
        case 'PENDING':
            return 'bg-yellow-500 hover:bg-yellow-600 text-white border-transparent';
        default:
            return '';
    }
}

const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString();
}

onMounted(() => {
    fetchMyOrders();
});
</script>

<template>
    <div class="p-6 max-w-4xl mx-auto space-y-8">
        <div>
            <h2 class="text-3xl font-extrabold tracking-tight">My Orders</h2>
            <p class="text-muted-foreground mt-2">Track your past and current orders.</p>
        </div>
        
        <div v-if="loading" class="text-center py-12">
             <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
             <p class="mt-4 text-muted-foreground">Loading orders...</p>
        </div>
        
        <div v-else-if="error" class="text-destructive font-medium p-4 bg-destructive/10 rounded-lg">
            {{ error }}
        </div>

        <div v-else-if="orders.length === 0" class="text-center py-12 bg-muted/20 rounded-xl border border-dashed">
            <p class="text-muted-foreground">You haven't placed any orders yet.</p>
            <Button variant="link" class="mt-2" @click="$router.push('/menu')">Order some food</Button>
        </div>

        <div v-else class="space-y-6">
            <Card v-for="order in orders" :key="order.id" class="overflow-hidden transition-all hover:shadow-md">
                <CardHeader class="bg-muted/30 pb-4">
                    <div class="flex justify-between items-start">
                        <div>
                            <CardTitle class="text-xl">Order #{{ order.id }}</CardTitle>
                            <CardDescription class="mt-1">
                                {{ formatDate(order.createdAt) }}
                            </CardDescription>
                        </div>
                        <Badge :variant="getStatusVariant(order.status)" :class="getStatusColorClass(order.status)">
                            {{ order.status }}
                        </Badge>
                    </div>
                </CardHeader>
                
                <CardContent class="p-6">
                    <div v-if="!order.items || order.items.length === 0" class="text-sm text-muted-foreground italic">
                         No items details available.
                    </div>
                    <ul v-else class="space-y-3">
                        <li v-for="item in order.items" :key="item.id" class="flex justify-between items-center text-sm">
                            <div class="flex items-center gap-3">
                                <Badge variant="outline" class="h-6 w-6 flex items-center justify-center rounded-sm p-0 border-muted-foreground/30">
                                    {{ item.quantity }}
                                </Badge>
                                <span class="font-medium">{{ item.snapshotName }}</span>
                            </div>
                            <span class="text-muted-foreground font-medium">
                                ${{ (item.snapshotPrice * item.quantity).toFixed(2) }}
                            </span>
                        </li>
                    </ul>
                </CardContent>

                <Separator />
                
                <CardFooter class="p-6 bg-muted/10 flex flex-row items-center justify-between">
                    <div class="flex flex-col">
                        <span class="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Total Amount</span>
                        <span class="text-2xl font-bold">${{ order.totalPrice.toFixed(2) }}</span>
                    </div>
                    
                    <Button 
                        v-if="order.status === 'PENDING'" 
                        @click="payOrder(order.id)" 
                        size="lg"
                        class="bg-blue-600 hover:bg-blue-700 shadow-md"
                    >
                        Pay Now
                    </Button>
                     <div v-else-if="order.status === 'PAID' || order.status === 'COMPLETED'" class="flex items-center text-emerald-600 font-medium text-sm">
                        <svg class="w-5 h-5 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                        Paid
                    </div>
                </CardFooter>
            </Card>
        </div>
    </div>
</template>
