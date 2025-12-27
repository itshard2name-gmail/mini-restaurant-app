<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useCartStore } from '../stores/cart';
import { Card, CardContent, CardTitle, CardFooter, Badge, Button, AspectRatio } from '@mini-restaurant/ui';

const cartStore = useCartStore();
const { addToCart } = cartStore;

const menus = ref([]);
const categories = ref([]);
const errorMessage = ref('');
const searchQuery = ref('');

const selectedCategoryId = ref('All');

// Computed Categories for UI (All + Fetched)
const displayCategories = computed(() => {
    // Sort categories by displayOrder
    const sorted = [...categories.value].sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0));
    return [{ id: 'All', name: 'All' }, ...sorted];
});

const filteredMenus = computed(() => {
    const lowerQuery = searchQuery.value.toLowerCase();
    
    return menus.value.filter(menu => {
        // Filter 1: Search Query
        const matchesSearch = !searchQuery.value || 
            menu.name.toLowerCase().includes(lowerQuery) || 
            (menu.description && menu.description.toLowerCase().includes(lowerQuery));

        // Filter 2: Category
        // Compare menu.category.id with selectedCategoryId
        const matchesCategory = selectedCategoryId.value === 'All' || 
            (menu.category?.id === selectedCategoryId.value);

        return matchesSearch && matchesCategory;
    });
});

const fetchData = async () => {
  try {
    const [menuRes, catRes] = await Promise.all([
        axios.get('/api/orders/menus'),
        axios.get('/api/orders/categories')
    ]);
    menus.value = menuRes.data;
    categories.value = catRes.data;
  } catch (error) {
    console.error('Error fetching data:', error);
    errorMessage.value = 'Failed to load menu data.';
  }
};

onMounted(() => {
  fetchData();
});
const emit = defineEmits(['added-to-cart']);

import { computed } from 'vue';

const isAdmin = computed(() => {
  const token = localStorage.getItem('token');
  if (!token) return false;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.roles && payload.roles.includes('ROLE_ADMIN');
  } catch (e) {
    return false;
  }
});

const addToCartHandler = (menu) => {
    addToCart(menu);
    emit('added-to-cart', menu.name);
};
</script>

<template>
    <div>
        <div v-if="errorMessage" class="mb-6 p-4 bg-destructive/15 text-destructive rounded-lg font-medium">
            {{ errorMessage }}
        </div>

        <div class="mb-6 space-y-4">
             <!-- Search Bar -->
             <div class="relative">
                 <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="h-5 w-5 text-muted-foreground" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                 </div>
                 <input 
                    v-model="searchQuery" 
                    type="text" 
                    placeholder="Search menu items..." 
                    class="w-full pl-10 pr-4 py-2 rounded-lg border border-input bg-background/50 ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 transition-all shadow-sm"
                 >
             </div>

             <!-- Category Filter Pills -->
             <div class="flex flex-wrap gap-2">
                 <button 
                    v-for="cat in displayCategories" 
                    :key="cat.id"
                    @click="selectedCategoryId = cat.id"
                    class="px-4 py-1.5 rounded-full text-sm font-medium transition-all"
                    :class="selectedCategoryId === cat.id 
                        ? 'bg-primary text-primary-foreground shadow-sm' 
                        : 'bg-muted text-muted-foreground hover:bg-muted/80'"
                 >
                     {{ cat.name }}
                 </button>
             </div>
        </div>

        <div v-if="filteredMenus.length === 0" class="text-center py-12 text-muted-foreground">
            <p>No menu items found.</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <Card v-for="menu in filteredMenus" :key="menu.id" class="border-0 shadow-md hover:shadow-xl transition-all duration-300 group overflow-hidden bg-card text-card-foreground">
                <div class="relative">
                     <AspectRatio :ratio="16/9" class="bg-muted">
                        <img 
                            :src="menu.imageUrl" 
                            :alt="menu.name" 
                            class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                        >
                     </AspectRatio>
                     <div class="absolute top-2 right-2">
                         <Badge variant="secondary" class="bg-primary text-primary-foreground font-bold shadow-sm border-0">
                             ${{ menu.price }}
                         </Badge>
                     </div>
                </div>
                
                <CardContent class="p-4">
                    <CardTitle class="text-xl mb-2 line-clamp-1">{{ menu.name }}</CardTitle>
                    <p class="text-muted-foreground text-sm line-clamp-2 h-10">
                        {{ menu.description }}
                    </p>
                </CardContent>

                <CardFooter class="p-4 pt-0" v-if="!isAdmin">
                    <Button @click="addToCartHandler(menu)" class="w-full font-semibold shadow-sm hover:shadow-md transition-all active:scale-95">
                        Add to Cart
                    </Button>
                </CardFooter>
            </Card>
        </div>
    </div>
</template>
