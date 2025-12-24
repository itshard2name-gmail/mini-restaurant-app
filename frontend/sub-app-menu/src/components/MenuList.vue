<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useCartStore } from '../stores/cart';
import { Card, CardContent, CardTitle, CardFooter } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { AspectRatio } from '@/components/ui/aspect-ratio';

const cartStore = useCartStore();
const { addToCart } = cartStore;

const menus = ref([]);
const errorMessage = ref('');
const searchQuery = ref('');

const selectedCategory = ref('All');

const categories = computed(() => {
    // Dynamically extra categories from loaded menus
    const cats = new Set(menus.value.map(m => m.category || 'Main')); // Default to Main if null
    return ['All', ...Array.from(cats)];
});

const filteredMenus = computed(() => {
    const lowerQuery = searchQuery.value.toLowerCase();
    
    return menus.value.filter(menu => {
        // Filter 1: Search Query
        const matchesSearch = !searchQuery.value || 
            menu.name.toLowerCase().includes(lowerQuery) || 
            (menu.description && menu.description.toLowerCase().includes(lowerQuery));

        // Filter 2: Category
        const matchesCategory = selectedCategory.value === 'All' || 
            (menu.category || 'Main') === selectedCategory.value;

        return matchesSearch && matchesCategory;
    });
});

const fetchMenus = async () => {
  try {
    const response = await axios.get('/api/orders/menus');
    menus.value = response.data;
  } catch (error) {
    console.error('Error fetching menus:', error);
    errorMessage.value = 'Failed to load menus.';
  }
};

onMounted(() => {
  fetchMenus();
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
                    v-for="cat in categories" 
                    :key="cat"
                    @click="selectedCategory = cat"
                    class="px-4 py-1.5 rounded-full text-sm font-medium transition-all"
                    :class="selectedCategory === cat 
                        ? 'bg-primary text-primary-foreground shadow-sm' 
                        : 'bg-muted text-muted-foreground hover:bg-muted/80'"
                 >
                     {{ cat }}
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
                         <Badge variant="secondary" class="bg-white/90 backdrop-blur text-foreground font-bold shadow-sm">
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
