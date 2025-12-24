<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';

const activeOrders = ref([]);
const historyOrders = ref([]);
const currentTab = ref('active'); // 'active' | 'history'

const loading = ref(false);
const historyLoading = ref(false); // loading state for load more
const error = ref('');

// Pagination for history
const historyPage = ref(0);
const historySize = ref(5);
const hasMoreHistory = ref(true);

const fetchActiveOrders = async () => {
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
        const response = await axios.get('/api/orders/my/active', {
             headers: {
                'Authorization': `Bearer ${token}`,
                'X-User-Id': userId
             }
        });
        activeOrders.value = response.data;
    } catch (e) {
        console.error('Failed to fetch active orders', e);
        error.value = 'Failed to load active orders.';
    } finally {
        loading.value = false;
    }
};

const fetchHistoryOrders = async (reset = false) => {
    if (reset) {
        historyPage.value = 0;
        historyOrders.value = [];
        hasMoreHistory.value = true;
    }
    
    if (!hasMoreHistory.value) return;

    historyLoading.value = true;
    
    const token = localStorage.getItem('token');
    let userId = 'admin';
    if (token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            userId = payload.sub || 'admin';
        } catch(e) {}
    }

    try {
        const response = await axios.get(`/api/orders/my/history?page=${historyPage.value}&size=${historySize.value}`, {
             headers: {
                'Authorization': `Bearer ${token}`,
                'X-User-Id': userId
             }
        });
        
        const newOrders = response.data.content;
        if (newOrders.length < historySize.value || response.data.last) {
            hasMoreHistory.value = false;
        }
        
        if (reset) {
            historyOrders.value = newOrders;
        } else {
            historyOrders.value = [...historyOrders.value, ...newOrders];
        }
        
        if (newOrders.length > 0) {
             historyPage.value++;
        }
        
    } catch (e) {
        console.error('Failed to fetch history orders', e);
        error.value = 'Failed to load history orders.';
    } finally {
        historyLoading.value = false;
    }
};

const switchTab = (tab) => {
    currentTab.value = tab;
    if (tab === 'active') {
        fetchActiveOrders();
    } else {
        if (historyOrders.value.length === 0) {
            fetchHistoryOrders(true); // Initial load
        }
    }
}

const payOrder = async (orderId) => {
    const token = localStorage.getItem('token');
    try {
        await axios.patch(`/api/orders/${orderId}/pay`, {}, {
             headers: {
                'Authorization': `Bearer ${token}`
             }
        });
        // Refresh active orders
        fetchActiveOrders();
    } catch (e) {
        console.error('Payment failed', e);
        alert('Payment failed');
    }
};

const showCancelDialog = ref(false);
const orderToCancel = ref(null);

const confirmCancel = async () => {
    if (!orderToCancel.value) return;
    
    const token = localStorage.getItem('token');
    const userId = getUserIdFromToken(token);

    try {
        await axios.patch(`/api/orders/${orderToCancel.value}/cancel`, {}, {
             headers: {
                'Authorization': `Bearer ${token}`,
                'X-User-Id': userId
             }
        });
        // Refresh active orders
        fetchActiveOrders();
        showCancelDialog.value = false;
        orderToCancel.value = null;
    } catch (e) {
        console.error('Cancellation failed', e);
        alert('Cancellation failed: ' + (e.response?.data?.message || 'Unknown error'));
        showCancelDialog.value = false;
    }
};

const openCancelDialog = (orderId) => {
    orderToCancel.value = orderId;
    showCancelDialog.value = true;
};

const getUserIdFromToken = (token) => {
    if (!token) return 'admin';
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.sub || 'admin';
    } catch(e) {
        return 'admin';
    }
};

const getStatusVariant = (status) => {
    switch (status) {
        case 'PAID':
        case 'COMPLETED':
            return 'default'; 
        case 'PENDING':
        case 'PREPARING':
            return 'secondary';
        case 'READY':
            return 'outline'; 
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
        case 'PREPARING':
            return 'bg-blue-500 hover:bg-blue-600 text-white border-transparent';
        case 'READY':
            return 'bg-orange-500 hover:bg-orange-600 text-white border-transparent animate-pulse';
        default:
            return '';
    }
}

const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString();
}

let stompClient = null;

const connectWebSocket = () => {
    const token = localStorage.getItem('token');
    let userId = 'admin';
    if (token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            userId = payload.sub || 'admin';
        } catch(e) {}
    } else {
        return; 
    }

    console.log('Connecting to WebSocket for user:', userId);
    const socket = new SockJS('http://localhost:8088/ws'); 
    stompClient = new Client({
        webSocketFactory: () => socket,
        debug: (str) => {
            console.log('STOMP: ' + str);
        },
        onConnect: () => {
            console.log('Connected to WebSocket');
            stompClient.subscribe(`/topic/orders/user/${userId}`, (message) => {
                if (message.body) {
                    console.log('Received order update:', message.body);
                    fetchActiveOrders(); 
                    if (currentTab.value === 'history') {
                         fetchHistoryOrders(true); 
                    }
                }
            });
        },
        onStompError: (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        },
    });

    stompClient.activate();
};

onMounted(() => {
    fetchActiveOrders();
    connectWebSocket();
});

onUnmounted(() => {
    if (stompClient) {
        stompClient.deactivate();
        console.log('Disconnected WebSocket');
    }
});
</script>

<template>
    <div id="sub-app-menu" class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 relative">
        <div>
            <h2 class="text-3xl font-extrabold tracking-tight">My Orders</h2>
            <p class="text-muted-foreground mt-2">Track your past and current orders.</p>
        </div>
        
        <!-- Tabs Implementation -->
        <div class="flex space-x-2 border-b">
             <button 
                @click="switchTab('active')"
                class="px-4 py-2 font-medium text-sm transition-colors relative"
                :class="currentTab === 'active' ? 'text-primary border-b-2 border-primary -mb-px' : 'text-muted-foreground hover:text-foreground'"
             >
                Active Orders
                <Badge v-if="activeOrders.length > 0" class="ml-2 px-1.5 py-0.5 text-[10px]">{{ activeOrders.length }}</Badge>
             </button>
             <button 
                @click="switchTab('history')"
                class="px-4 py-2 font-medium text-sm transition-colors relative"
                :class="currentTab === 'history' ? 'text-primary border-b-2 border-primary -mb-px' : 'text-muted-foreground hover:text-foreground'"
             >
                Order History
             </button>
        </div>

        <div v-if="loading && currentTab === 'active' && activeOrders.length === 0" class="text-center py-12">
             <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
             <p class="mt-4 text-muted-foreground">Loading orders...</p>
        </div>
        
        <div v-else-if="error" class="text-destructive font-medium p-4 bg-destructive/10 rounded-lg">
            {{ error }}
        </div>

        <!-- Orders List -->
        <div v-else>
            <div v-if="currentTab === 'active' && activeOrders.length === 0" class="text-center py-12 bg-muted/20 rounded-xl border border-dashed">
                 <p class="text-muted-foreground">No active orders.</p>
                 <Button variant="link" class="mt-2" @click="$router.push('/menu')">Order now</Button>
            </div>
             <div v-if="currentTab === 'history' && historyOrders.length === 0 && !historyLoading" class="text-center py-12 bg-muted/20 rounded-xl border border-dashed">
                 <p class="text-muted-foreground">No order history found.</p>
            </div>

            <div class="space-y-6">
                 <!-- Use computed property or just v-for based on tab -->
                <Card v-for="order in (currentTab === 'active' ? activeOrders : historyOrders)" :key="order.id" class="overflow-hidden transition-all hover:shadow-md">
                    <CardHeader class="bg-muted/30 pb-4">
                        <div class="flex justify-between items-start">
                            <div>
                                <div class="flex items-center gap-2 mb-1">
                                    <Badge v-if="order.orderType === 'DINE_IN'" variant="secondary" class="bg-indigo-50 text-indigo-700 border-indigo-100 flex items-center gap-1 px-2 py-0.5 text-[10px]">
                                          <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2v0a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/></svg>
                                          <span class="font-bold">Table {{ order.tableNumber }}</span>
                                    </Badge>
                                    <Badge v-if="order.orderType === 'TAKEOUT'" variant="secondary" class="bg-emerald-50 text-emerald-700 border-emerald-100 flex items-center gap-1 px-2 py-0.5 text-[10px]">
                                          <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="18" x2="23" y1="8" y2="13"/><line x1="23" x2="18" y1="13" y2="13"/></svg>
                                          <span class="font-bold">Takeout</span>
                                    </Badge>
                                </div>
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
                            <li v-for="item in order.items" :key="item.id" class="flex flex-col gap-1 py-2 border-b border-border/40 last:border-0 text-sm">
                                <div class="flex justify-between items-center w-full">
                                    <div class="flex items-center gap-3">
                                        <Badge variant="outline" class="h-6 w-6 flex items-center justify-center rounded-sm p-0 border-muted-foreground/30">
                                            {{ item.quantity }}
                                        </Badge>
                                        <span class="font-medium">{{ item.snapshotName }}</span>
                                    </div>
                                    <span class="text-muted-foreground font-medium">
                                        ${{ (item.snapshotPrice * item.quantity).toFixed(2) }}
                                    </span>
                                </div>
                                <div v-if="item.notes" class="pl-9 text-xs text-muted-foreground flex items-start gap-1">
                                    <span class="uppercase tracking-wider text-[10px] font-semibold text-muted-foreground/70 mt-0.5">Note:</span>
                                    <span>{{ item.notes }}</span>
                                </div>
                            </li>
                        </ul>
                    </CardContent>
    
                    <Separator />
                    
                    <CardFooter class="p-6 bg-muted/10 flex flex-row items-center justify-between">
                        <div class="flex flex-col">
                            <span class="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Total Amount</span>
                            <span class="text-2xl font-bold">${{ order.totalPrice.toFixed(2) }}</span>
                        </div>
                        
                        <div v-if="order.status === 'PENDING'" class="flex gap-2">
                            <Button 
                                @click="openCancelDialog(order.id)" 
                                variant="outline"
                                class="text-destructive hover:bg-destructive/10 border-destructive/50 h-10 px-4 py-2"
                            >
                                Cancel Order
                            </Button>
                            <Button 
                                @click="payOrder(order.id)" 
                                class="bg-blue-600 hover:bg-blue-700 shadow-md h-10 px-4 py-2"
                            >
                                Pay Now
                            </Button>
                        </div>
                         <div v-else-if="order.status === 'PAID' || order.status === 'COMPLETED'" class="flex items-center text-emerald-600 font-medium text-sm">
                            <svg class="w-5 h-5 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                            Paid
                        </div>
                    </CardFooter>
                </Card>
            </div>
            
            <!-- Load More Button for History -->
             <div v-if="currentTab === 'history' && hasMoreHistory" class="text-center pt-4">
                <Button variant="outline" @click="fetchHistoryOrders()" :disabled="historyLoading">
                     <span v-if="historyLoading" class="mr-2 animate-spin">⟳</span>
                     {{ historyLoading ? 'Loading more...' : 'Load More History' }}
                </Button>
             </div>
        </div>

        <!-- Custom Confirmation Dialog -->
        <div v-if="showCancelDialog" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
             <div class="bg-background rounded-lg shadow-lg max-w-sm w-full border border-border">
                 <div class="p-6 space-y-2">
                     <h3 class="text-lg font-semibold leading-none tracking-tight">Cancel Order?</h3>
                     <p class="text-sm text-muted-foreground">
                         Are you sure you want to cancel Order #{{ orderToCancel }}? This action cannot be undone.
                     </p>
                 </div>
                 <div class="flex items-center justify-end p-6 pt-0 gap-2">
                     <Button variant="outline" @click="showCancelDialog = false">Keep Order</Button>
                     <Button variant="destructive" @click="confirmCancel">Yes, Cancel</Button>
                 </div>
             </div>
        </div>
    </div>
</template>
