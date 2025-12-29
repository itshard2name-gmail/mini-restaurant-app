<template>
  <div>
      <!-- Header Section -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h2 class="text-2xl font-bold text-foreground tracking-tight">Active Orders</h2>
          <p class="mt-1 text-sm text-muted-foreground">Real-time order tracking</p>
        </div>
        <div class="flex gap-2">
            <Badge variant="secondary" class="px-5 py-2.5 text-sm font-semibold shadow-md bg-primary text-primary-foreground hover:bg-primary/90 border border-primary/30">
            {{ orders.filter(o => ['PENDING', 'PAID', 'PREPARING', 'READY'].includes(o.status)).length }} Active
            </Badge>
            <Badge variant="outline" class="px-5 py-2.5 text-sm font-semibold shadow-sm text-muted-foreground border-border bg-card">
            {{ totalElements }} Total
            </Badge>
        </div>
      </div>
      
      <!-- Smart Search Bar -->
      <div class="mb-6 bg-card p-4 rounded-xl border border-border shadow-sm flex flex-col md:flex-row gap-4 items-center justify-between">
          <div class="relative w-full md:w-96">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-muted-foreground" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd" />
                </svg>
              </div>
              <input 
                v-model="searchQuery" 
                type="text" 
                placeholder="Search Order ID or Customer Name..." 
                class="block w-full pl-10 pr-3 py-2 border border-input rounded-md leading-5 bg-background text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:border-ring sm:text-sm transition duration-150 ease-in-out"
              >
          </div>
          
          <div class="flex gap-4 w-full md:w-auto">
             <input 
               v-model="dateFilter"
               type="date" 
               class="block w-full md:w-auto pl-3 pr-10 py-2 border border-input rounded-md leading-5 bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:border-ring sm:text-sm"
             >
             <Button 
               v-if="searchQuery || dateFilter"
               @click="clearFilters"
               variant="ghost" 
               class="text-red-500 hover:text-red-700 hover:bg-red-50"
             >
                Clear
             </Button>
          </div>
      </div>

      <!-- Search Results Banner -->
      <div v-if="isSearchActive" class="mb-6 bg-indigo-50 border border-indigo-100 text-indigo-700 px-4 py-3 rounded-lg flex items-center">
         <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd" />
         </svg>
         <span class="font-medium">Search Mode Code:</span>
         <span class="ml-1">Showing {{ totalElements }} results for filters. Tabs are temporarily hidden.</span>
      </div>

      <!-- Tabs Navigation (Hidden when searching) -->
      <div v-if="!isSearchActive" class="mb-8 border-b border-border">
        <nav class="-mb-px flex space-x-8" aria-label="Tabs">
          <button
            v-for="tab in tabs"
            :key="tab.name"
            @click="currentTab = tab.name"
            :class="[
              currentTab === tab.name
                ? 'border-primary text-primary'
                : 'border-transparent text-muted-foreground hover:text-foreground hover:border-border',
              'whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm flex items-center gap-2 transition-colors'
            ]"
          >
            {{ tab.label }}
            <span
              v-if="getOrderCount(tab.filters) > 0"
              :class="[
                currentTab === tab.name ? 'bg-indigo-900/50 text-indigo-300' : 'bg-gray-800 text-gray-400',
                'hidden ml-2 py-0.5 px-2.5 rounded-full text-xs font-medium md:inline-block'
              ]"
            >
              {{ getOrderCount(tab.filters) }}
            </span>
          </button>
        </nav>
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
      
      <!-- Empty State -->
      <div v-else-if="orders.length === 0" class="text-center py-20 bg-gray-50 rounded-lg border-2 border-dashed border-gray-200">
         <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
             <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
         </svg>
         <h3 class="mt-2 text-sm font-medium text-gray-900">No orders found</h3>
         <p class="mt-1 text-sm text-gray-500">There are no orders in this category.</p>
      </div>

      <!-- Orders Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        
        <Card v-for="order in orders" :key="order.id" class="shadow-lg hover:shadow-2xl hover:shadow-primary/20 transition-all duration-300 border-border bg-card overflow-hidden group flex flex-col">
          
          <!-- Card Header -->
          <CardHeader class="px-6 py-5 border-b border-border bg-card relative space-y-0 block">
             <div class="flex justify-between items-start">
               <div>
                  <span class="block text-xs font-bold text-muted-foreground uppercase tracking-wider mb-1">Order ID</span>
                  <CardTitle class="text-3xl font-black text-card-foreground leading-none tracking-tight">#{{ order.id }}</CardTitle>
                  <CardDescription class="mt-1 text-xs text-muted-foreground font-medium">
                    {{ formatDate(order.createdAt) }}
                  </CardDescription>
               </div>
               <Badge 
                  :variant="statusVariant(order.status)" 
                  class="px-3 py-1 text-xs font-bold rounded-full uppercase tracking-wider"
                  :class="{
                    'bg-orange-100 text-orange-800 border-orange-200 dark:bg-orange-900/40 dark:text-orange-200 dark:border-orange-700/50 hover:bg-orange-100 dark:hover:bg-orange-900/60': order.status === 'READY',
                     'bg-yellow-100 text-yellow-800 border-yellow-200 dark:bg-yellow-900/40 dark:text-yellow-200 dark:border-yellow-700/50 hover:bg-yellow-100 dark:hover:bg-yellow-900/60': order.status === 'PREPARING'
                   }"
                >
                   {{ order.status }}
                </Badge>

              </div>
              
              <!-- Dining Mode Badge -->
              <div class="mt-4 flex items-center">
                  <Badge v-if="order.orderType === 'DINE_IN'" variant="secondary" class="bg-primary text-primary-foreground shadow-sm flex items-center gap-1.5 px-3 py-1 border border-primary/50">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2v0a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/></svg>
                      <span class="font-bold">Table {{ order.tableNumber }}</span>
                  </Badge>
                  
                  <Badge v-if="order.orderType === 'TAKEOUT'" variant="secondary" class="bg-emerald-100 text-emerald-800 border-emerald-200 dark:bg-emerald-900/30 dark:text-emerald-300 dark:border-emerald-500/30 flex items-center gap-1.5 px-3 py-1 border">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="18" x2="23" y1="8" y2="13"/><line x1="23" x2="18" y1="13" y2="13"/></svg>
                      <span class="font-bold">Takeout</span>
                  </Badge>
              </div>
          </CardHeader>

          <!-- Card Content -->
          <CardContent class="px-6 py-6 flex-1 flex flex-col space-y-6">
            
            <!-- Customer Info -->
            <div>
              <span class="block text-xs font-bold text-muted-foreground uppercase tracking-wider mb-3">Customer</span>
              <div class="flex items-center">
                 <div class="h-10 w-10 rounded-full bg-gradient-to-br from-indigo-600 to-purple-800 flex items-center justify-center shadow-lg text-white font-bold text-sm shrink-0 border border-white/10">
                    {{ order.userId ? order.userId.charAt(0).toUpperCase() : 'U' }}
                 </div>
                 <div class="ml-3">
                   <p class="text-sm font-bold text-card-foreground truncate">{{ order.userId || 'Guest User' }}</p>
                   <p class="text-xs text-muted-foreground">Online Customer</p>
                 </div>
              </div>
            </div>

            <!-- Items List -->
            <div class="flex-1">
              <span class="block text-xs font-bold text-muted-foreground uppercase tracking-wider mb-3">Order Items</span>
              <ul class="space-y-2">
                <li v-for="item in order.items" :key="item.id" class="flex flex-col text-sm p-3 rounded-lg bg-background/50 border border-border">
                  <div class="flex items-center justify-between w-full">
                    <span class="font-medium text-foreground truncate mr-2">{{ item.snapshotName }}</span>
                    <Badge variant="outline" class="flex-shrink-0 bg-background px-2 py-0.5 text-xs font-bold text-muted-foreground border-border shadow-sm">x{{ item.quantity }}</Badge>
                  </div>
                  <div v-if="item.notes" class="mt-2 text-xs text-gray-400 italic bg-gray-800 p-1.5 rounded border border-gray-700/50">
                    <span class="font-bold not-italic text-indigo-400">Note:</span> {{ item.notes }}
                  </div>
                </li>
              </ul>
            </div>
          </CardContent>

          <!-- Card Footer -->
          <CardFooter class="px-6 py-5 bg-muted/50 border-t border-border flex flex-col justify-end items-stretch">
             <div class="flex justify-between items-center mb-5 w-full">
                <span class="text-xs font-bold text-muted-foreground uppercase tracking-wide">Total Amount</span>
                <span class="text-2xl font-black text-white">${{ order.totalPrice.toFixed(2) }}</span>
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
                  @click="updateStatus(order.id, 'READY')"
                  class="w-full bg-orange-500 hover:bg-orange-600 text-white font-semibold shadow-md"
                >
                  Mark Ready for Pickup
                </Button>
                <Button
                  v-if="order.status === 'READY'"
                  @click="updateStatus(order.id, 'COMPLETED')"
                  class="w-full bg-emerald-500 hover:bg-emerald-600 text-white font-semibold shadow-md"
                >
                  Complete Order
                </Button>
                
                <!-- Completed State -->
                <div v-if="order.status === 'COMPLETED'" class="flex items-center justify-center py-2 text-sm text-emerald-400 font-medium bg-emerald-900/10 rounded-lg border border-emerald-900/30">
                   <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                   <span>Order has been fulfilled</span>
                </div>
                <!-- Cancelled State -->
                 <div v-if="order.status === 'CANCELLED'" class="flex items-center justify-center py-2 text-sm text-red-400 font-medium bg-red-900/10 rounded-lg border border-red-900/30">
                   <span>Order Cancelled</span>
                </div>
             </div>
          </CardFooter>
        </Card>

      </div>

      <!-- Pagination Controls -->
      <div v-if="totalPages > 1 && !loading" class="mt-8 flex justify-center gap-4">
          <Button 
            :disabled="page === 0" 
            @click="page = page - 1"
            variant="outline"
          >
            Previous
          </Button>
          <span class="py-2 text-sm text-gray-600 font-medium">
             Page {{ page + 1 }} of {{ totalPages }}
          </span>
          <Button 
            :disabled="page >= totalPages - 1" 
            @click="page = page + 1"
            variant="outline"
          >
            Next
          </Button>
      </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import axios from 'axios';
import { Client } from '@stomp/stompjs';

// Shadcn UI Components
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter, Button, Badge, toast } from '@mini-restaurant/ui';

const orders = ref([]);
const loading = ref(true);
const error = ref(null);

// Search & Filter State
const searchQuery = ref('');
const dateFilter = ref('');

// Pagination State
const page = ref(0);
const size = ref(20);
const totalPages = ref(0);
const totalElements = ref(0);

const currentTab = ref('active');

const tabs = [
    { name: 'active', label: 'All Active', filters: ['PENDING', 'PAID', 'PREPARING', 'READY'] },
    { name: 'pending', label: 'Pending', filters: ['PENDING', 'PAID'] },
    { name: 'kitchen', label: 'Kitchen', filters: ['PREPARING'] },
    { name: 'counter', label: 'Counter', filters: ['READY'] },
    { name: 'history', label: 'History', filters: ['COMPLETED', 'CANCELLED'] },
];

const isSearchActive = computed(() => {
    return searchQuery.value.trim() !== '' || dateFilter.value !== '';
});

// Debounce helper
const debounce = (fn, delay) => {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => fn(...args), delay);
    };
};

const fetchOrders = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    
    const params = {
        page: page.value,
        size: size.value,
        query: searchQuery.value || null,
        date: dateFilter.value || null
    };

    // If not searching, apply Status Filter based on Tab
    if (!isSearchActive.value) {
        const filters = activeTabFilter();
        if (filters && filters.length > 0) {
             // Pass as comma-separated string which axios/Spring handles as List
             params.status = filters.join(',');
        }
    }
    
    const response = await axios.get('/api/orders/admin/search', {
      params,
      headers: { Authorization: `Bearer ${token}` }
    });

    // Response is Page<Order>
    orders.value = response.data.content;
    totalPages.value = response.data.totalPages;
    totalElements.value = response.data.totalElements;
    
  } catch (err) {
    console.error('Failed to fetch orders:', err);
    error.value = 'Failed to load orders.';
  } finally {
      loading.value = false;
  }
};

const debouncedFetch = debounce(() => {
    page.value = 0; // Reset to first page on filter change
    fetchOrders();
}, 500);

// --- WATCHERS ---
watch(searchQuery, debouncedFetch);

watch(dateFilter, () => {
    page.value = 0;
    fetchOrders();
});

watch(currentTab, () => {
    page.value = 0;
    fetchOrders();
});

watch(page, () => {
    fetchOrders();
});

const clearFilters = () => {
    searchQuery.value = '';
    dateFilter.value = '';
};

const getOrderCount = (filters) => {
    // Note: With server-side pagination, we can't easily get counts for other tabs without extra API calls.
    // We will hide the counts for now or implement a separate 'counts' endpoint later if needed.
    return 0; // Placeholder to hide numbers
}

const activeTabFilter = () => {
    return tabs.find(t => t.name === currentTab.value)?.filters;
}

const statusVariant = (status) => {
  const variants = {
    'PENDING': 'secondary',
    'PAID': 'default',     
    'PREPARING': 'secondary',
    'READY': 'outline', 
    'COMPLETED': 'outline',
    'CANCELLED': 'destructive',
  };
  return variants[status] || 'secondary';
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
    toast.error('Failed to update order status');
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
    // Dynamic WebSocket URL for Admin Portal
    // Uses Nginx /ws/ proxy setup
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${wsProtocol}//${window.location.host}/ws`;

    stompClient = new Client({
        brokerURL: wsUrl, 
        onConnect: () => {
            console.log('Connected to WebSocket via ' + wsUrl);
            stompClient.subscribe('/topic/orders', (message) => {
                console.log('Received message:', message.body);
                // Refresh current page on update
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
  connectWebSocket();
});

onUnmounted(() => {
  if (stompClient) stompClient.deactivate();
});
</script>
