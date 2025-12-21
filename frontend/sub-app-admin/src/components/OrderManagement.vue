<template>
  <div>
      <!-- Header Section -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h2 class="text-2xl font-bold text-gray-900 tracking-tight">Active Orders</h2>
          <p class="mt-1 text-sm text-gray-500">Real-time order tracking</p>
        </div>
        <Badge variant="secondary" class="px-5 py-2.5 text-sm font-semibold shadow-md">
          {{ orders.length }} Active
        </Badge>
      </div>
      
      <!-- Loading State -->
      <div v-if="loading" class="text-center py-32">
        <div class="animate-spin rounded-full h-16 w-16 border-4 border-t-indigo-600 border-indigo-200 mx-auto"></div>
        <p class="mt-6 text-lg text-gray-600 font-medium">Syncing orders...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-white border-l-4 border-red-500 p-8 mb-12 rounded-lg shadow-lg">
        <div class="flex items-center">
          <div class="flex-shrink-0 bg-red-100 p-2 rounded-full">
            <svg class="h-6 w-6 text-red-600" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="ml-4">
            <h3 class="text-lg font-bold text-gray-900">Error Loading Data</h3>
            <p class="text-sm text-gray-600 mt-1">{{ error }}</p>
          </div>
        </div>
      </div>

      <!-- Orders Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        
        <Card v-for="order in orders" :key="order.id" class="shadow-sm hover:shadow-xl transition-all duration-300 border-gray-200 overflow-hidden group flex flex-col">
          
          <!-- Card Header -->
          <CardHeader class="px-6 py-5 border-b border-gray-100 bg-white relative space-y-0 block">
             <div class="flex justify-between items-start">
               <div>
                  <span class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">Order ID</span>
                  <CardTitle class="text-2xl font-black text-gray-800 leading-none">#{{ order.id }}</CardTitle>
                  <CardDescription class="mt-1 text-xs text-gray-500 font-medium">
                    {{ formatDate(order.createdAt) }}
                  </CardDescription>
               </div>
               <Badge :variant="statusVariant(order.status)" class="px-3 py-1 text-xs font-bold rounded-full uppercase tracking-wider">
                  {{ order.status }}
               </Badge>
             </div>
          </CardHeader>

          <!-- Card Content -->
          <CardContent class="px-6 py-6 flex-1 flex flex-col space-y-6">
            
            <!-- Customer Info -->
            <div>
              <span class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">Customer</span>
              <div class="flex items-center">
                 <div class="h-10 w-10 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-sm text-white font-bold text-sm shrink-0">
                    {{ order.userId ? order.userId.charAt(0).toUpperCase() : 'U' }}
                 </div>
                 <div class="ml-3">
                   <p class="text-sm font-bold text-gray-900 truncate">{{ order.userId || 'Guest User' }}</p>
                   <p class="text-xs text-gray-500">Online Customer</p>
                 </div>
              </div>
            </div>

            <!-- Items List -->
            <div class="flex-1">
              <span class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">Order Items</span>
              <ul class="space-y-2">
                <li v-for="item in order.items" :key="item.id" class="flex items-center justify-between text-sm p-2 rounded-lg bg-gray-50 border border-gray-100">
                  <span class="font-medium text-gray-700 truncate mr-2">{{ item.snapshotName }}</span>
                  <Badge variant="outline" class="flex-shrink-0 bg-white px-2 py-0.5 text-xs font-bold text-gray-600 border-gray-200 shadow-sm">x{{ item.quantity }}</Badge>
                </li>
              </ul>
            </div>
          </CardContent>

          <!-- Card Footer -->
          <CardFooter class="px-6 py-5 bg-gray-50 border-t border-gray-100 flex flex-col justify-end items-stretch">
             <div class="flex justify-between items-center mb-5 w-full">
                <span class="text-xs font-bold text-gray-500 uppercase tracking-wide">Total Amount</span>
                <span class="text-2xl font-black text-gray-900">${{ order.totalPrice }}</span>
             </div>

             <!-- Action Buttons -->
             <div class="space-y-3 w-full">
                <Button
                  v-if="order.status === 'PAID'"
                  @click="updateStatus(order.id, 'PREPARING')"
                  class="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold shadow-md"
                >
                  Accept Order
                </Button>
                <Button
                  v-if="order.status === 'PREPARING'"
                  @click="updateStatus(order.id, 'COMPLETED')"
                  class="w-full bg-emerald-500 hover:bg-emerald-600 text-white font-semibold shadow-md"
                >
                  Mark Ready Details
                </Button>
                
                <!-- Completed State -->
                <div v-if="order.status === 'COMPLETED'" class="flex items-center justify-center py-2 text-sm text-emerald-600 font-medium bg-emerald-50 rounded-lg border border-emerald-100">
                   <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                   <span>Order has been fulfilled</span>
                </div>
             </div>
          </CardFooter>
        </Card>

      </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';
import { Client } from '@stomp/stompjs';

// Shadcn UI Components
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

const orders = ref([]);
const loading = ref(true);
const error = ref(null);

const statusVariant = (status) => {
  const variants = {
    'PENDING': 'secondary',
    'PAID': 'default',     
    'PREPARING': 'secondary',
    'COMPLETED': 'outline',
    'CANCELLED': 'destructive',
  };
  return variants[status] || 'secondary';
};

const fetchOrders = async () => {
  try {
    const token = localStorage.getItem('token');
    const response = await axios.get('/api/orders/admin/all', {
      headers: { Authorization: `Bearer ${token}` }
    });
    orders.value = response.data;
    loading.value = false;
  } catch (err) {
    console.error('Failed to fetch orders:', err);
    error.value = 'Failed to load orders. Please ensure you are logged in as admin.';
    loading.value = false;
  }
};

const updateStatus = async (orderId, newStatus) => {
  try {
    const token = localStorage.getItem('token');
    await axios.patch(`/api/orders/admin/${orderId}/status`, 
      { status: newStatus },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    // Refresh orders
    await fetchOrders();
  } catch (err) {
    console.error('Failed to update status:', err);
    alert('Failed to update order status');
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString([], { 
    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' 
  });
};

let refreshInterval;
let stompClient = null;

const connectWebSocket = () => {
    stompClient = new Client({
        brokerURL: 'ws://localhost:8088/notification/ws',
        onConnect: () => {
            console.log('Connected to WebSocket');
            stompClient.subscribe('/topic/orders', (message) => {
                console.log('Received message:', message.body);
                fetchOrders();
            });
        },
        onWebSocketError: (error) => {
            console.error('WebSocket Error', error);
        },
        onStompError: (frame) => {
            console.error('Stomp Error', frame);
        }
    });

    stompClient.activate();
};

onMounted(() => {
  fetchOrders();
  refreshInterval = setInterval(fetchOrders, 10000);
  connectWebSocket();
});

onUnmounted(() => {
  if (refreshInterval) clearInterval(refreshInterval);
  if (stompClient) stompClient.deactivate();
});
</script>
