<template>
  <div>
    <!-- Header with Actions -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h2 class="text-2xl font-bold text-foreground tracking-tight">Menu Items</h2>
        <p class="mt-1 text-sm text-muted-foreground">Manage your food offerings</p>
      </div>
      <Button @click="openModal()" class="bg-indigo-600 hover:bg-indigo-700 text-white font-semibold shadow-sm border border-indigo-500/50">
        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
        Add New Item
      </Button>
    </div>

    <!-- Error State -->
    <div v-if="error" class="bg-red-50 border-l-4 border-red-500 p-4 mb-8">
      <div class="flex">
        <div class="ml-3">
          <p class="text-sm text-red-700">{{ error }}</p>
        </div>
      </div>
    </div>

    <!-- Menu Grid -->
    <div v-if="loading && items.length === 0" class="text-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-4 border-t-indigo-600 border-indigo-200 mx-auto"></div>
    </div>
    
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <Card v-for="item in items" :key="item.id" class="flex flex-col overflow-hidden hover:shadow-xl hover:shadow-primary/10 transition-all duration-300 bg-card border-border">
        <!-- Image placeholder or actual image -->
        <div class="w-full h-48 bg-muted relative overflow-hidden group">
            <img v-if="item.imageUrl" :src="item.imageUrl" alt="Menu Item" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" @error="$event.target.src='https://placehold.co/400x300/1f2937/fff?text=No+Image'" />
            <div v-else class="w-full h-full flex items-center justify-center text-gray-600">
                <span class="text-sm font-medium">No Image</span>
            </div>
            <div class="absolute top-2 right-2 flex space-x-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                <Button variant="secondary" size="sm" @click="openModal(item)" class="h-8 w-8 p-0 rounded-full bg-gray-800/90 hover:bg-gray-700 text-white shadow border border-gray-700">
                    <svg class="h-4 w-4 text-indigo-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                </Button>
                <Button variant="destructive" size="sm" @click="confirmDelete(item)" class="h-8 w-8 p-0 rounded-full bg-red-900/90 hover:bg-red-800 text-red-200 shadow border border-red-800">
                    <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                </Button>
            </div>
        </div>
        
        <CardContent class="p-5 flex-1 space-y-2">
            <div class="flex justify-between items-start">
                <h3 class="text-lg font-bold text-foreground line-clamp-1">{{ item.name }}</h3>
                <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-primary/10 text-primary border border-primary/20 mr-2">{{ item.category || 'Main' }}</span>
                <span class="text-lg font-black text-emerald-500">${{ item.price }}</span>
            </div>
            <p class="text-sm text-muted-foreground line-clamp-3">{{ item.description }}</p>
        </CardContent>
      </Card>
    </div>

    <!-- Edit/Add Modal -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
        <div class="bg-popover border border-border rounded-lg shadow-2xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200 scale-95">
            <div class="px-6 py-4 border-b border-border flex justify-between items-center bg-popover">
                <h3 class="text-lg font-bold text-foreground">{{ isEditing ? 'Edit Item' : 'New Menu Item' }}</h3>
                <button @click="closeModal" class="text-gray-400 hover:text-white transition-colors">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                </button>
            </div>
            <form @submit.prevent="saveItem">
                <div class="p-6 space-y-4">
                    <div class="space-y-2">
                        <label class="text-sm font-medium leading-none text-muted-foreground">Name</label>
                        <input v-model="formData.name" required class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring" placeholder="e.g. Cheese Burger">
                    </div>
                    <div class="space-y-2">
                        <label class="text-sm font-medium leading-none text-muted-foreground">Price ($)</label>
                        <input v-model="formData.price" type="number" step="0.01" required class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring" placeholder="0.00">
                    </div>
                    <div class="space-y-2">
                        <label class="text-sm font-medium leading-none text-muted-foreground">Category</label>
                        <select v-model="formData.category" class="flex h-10 w-full rounded-md border border-input bg-background text-foreground px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
                            <option value="Main">Main</option>
                            <option value="Starter">Starter</option>
                            <option value="Drink">Drink</option>
                            <option value="Dessert">Dessert</option>
                        </select>
                    </div>
                    <div class="space-y-2">
                        <label class="text-sm font-medium leading-none text-muted-foreground">Description</label>
                        <textarea v-model="formData.description" class="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring" placeholder="Describe the dish..."></textarea>
                    </div>
                    <div class="space-y-2">
                        <label class="text-sm font-medium leading-none text-muted-foreground">Image URL</label>
                        <div class="flex space-x-2">
                            <input v-model="formData.imageUrl" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring" placeholder="https://...">
                            <Button type="button" variant="outline" @click="generateRandomImage" class="whitespace-nowrap bg-primary/10 text-primary hover:bg-primary/20 border-primary/20" title="Auto-fill random image based on name">
                                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"></path></svg>
                                Magic Fill
                            </Button>
                        </div>
                        <p class="text-xs text-gray-500">Pro tip: Click 'Magic Fill' to auto-generate an image based on the item name.</p>
                    </div>
                </div>
                <div class="px-6 py-4 bg-popover border-t border-border flex justify-end space-x-2">
                    <Button type="button" variant="ghost" @click="closeModal" class="text-muted-foreground hover:text-foreground hover:bg-muted">Cancel</Button>
                    <Button type="submit" class="bg-indigo-600 hover:bg-indigo-700 text-white">Save Item</Button>
                </div>
            </form>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { Card, CardContent, Button } from '@mini-restaurant/ui';

const items = ref([]);
const loading = ref(true);
const error = ref(null);

const showModal = ref(false);
const isEditing = ref(false);
const formData = ref({
    id: null,
    name: '',
    price: '',
    category: 'Main',
    description: '',
    imageUrl: ''
});

const fetchMenus = async () => {
    try {
        const token = localStorage.getItem('token');
        // We use the new endpoint
        const response = await axios.get('/api/orders/menus', {
             headers: { Authorization: `Bearer ${token}` }
        });
        items.value = response.data;
        loading.value = false;
    } catch (err) {
        console.error('Failed to fetch menus:', err);
        error.value = 'Failed to load menu items.';
        loading.value = false;
    }
};

const generateRandomImage = () => {
    const keyword = formData.value.name || formData.value.category || 'food';
    const safeKeyword = encodeURIComponent(keyword.trim());
    // Use Pollinations.ai (Free AI Image Generation)
    // Add random param to force fresh generation if needed, though pollinations generates new seed by default usually.
    // Specifying nologo=true to cleaner images if supported, or just prompt.
    formData.value.imageUrl = `https://image.pollinations.ai/prompt/${safeKeyword}`;
};

const openModal = (item = null) => {
    isEditing.value = !!item;
    if (item) {
        formData.value = { ...item };
    } else {
        formData.value = { id: null, name: '', price: '', category: 'Main', description: '', imageUrl: '' };
    }
    showModal.value = true;
};

const closeModal = () => {
    showModal.value = false;
    formData.value = { id: null, name: '', price: '', category: 'Main', description: '', imageUrl: '' };
};

const saveItem = async () => {
    try {
        const token = localStorage.getItem('token');
        const payload = {
            name: formData.value.name,
            price: parseFloat(formData.value.price),
            category: formData.value.category,
            description: formData.value.description,
            imageUrl: formData.value.imageUrl
        };

        if (isEditing.value) {
            await axios.put(`/api/orders/menus/${formData.value.id}`, payload, {
                headers: { Authorization: `Bearer ${token}` }
            });
        } else {
            await axios.post('/api/orders/menus', payload, {
                headers: { Authorization: `Bearer ${token}` }
            });
        }
        await fetchMenus();
        closeModal();
    } catch (err) {
        console.error('Failed to save item:', err);
        alert('Failed to save item. Ensure you are Admin.');
    }
};

const confirmDelete = async (item) => {
    if (!confirm(`Are you sure you want to delete "${item.name}"?`)) return;
    try {
        const token = localStorage.getItem('token');
        await axios.delete(`/api/orders/menus/${item.id}`, {
             headers: { Authorization: `Bearer ${token}` }
        });
        await fetchMenus();
    } catch (err) {
        console.error('Failed to delete item:', err);
        alert('Failed to delete item.');
    }
};

onMounted(() => {
    fetchMenus();
});
</script>
