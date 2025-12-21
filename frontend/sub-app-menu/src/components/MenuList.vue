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

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <Card v-for="menu in menus" :key="menu.id" class="border-0 shadow-md hover:shadow-xl transition-all duration-300 group overflow-hidden bg-card text-card-foreground">
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
