<template>
  <div>
    <!-- Header with Actions -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 tracking-tight">Menu Items</h2>
        <p class="mt-1 text-sm text-gray-500">Manage your food offerings</p>
      </div>
      <Button @click="openModal()" class="bg-indigo-600 hover:bg-indigo-700 text-white font-semibold shadow-sm">
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
      <Card v-for="item in items" :key="item.id" class="flex flex-col overflow-hidden hover:shadow-lg transition-shadow duration-200">
        <!-- Image placeholder or actual image -->
        <div class="w-full h-48 bg-gray-200 relative overflow-hidden">
            <img v-if="item.imageUrl" :src="item.imageUrl" alt="Menu Item" class="w-full h-full object-cover" @error="$event.target.src='https://placehold.co/400x300?text=No+Image'" />
            <div v-else class="w-full h-full flex items-center justify-center text-gray-400">
                <span class="text-sm font-medium">No Image</span>
            </div>
            <div class="absolute top-2 right-2 flex space-x-1">
                <Button variant="secondary" size="sm" @click="openModal(item)" class="h-8 w-8 p-0 rounded-full bg-white/90 hover:bg-white shadow">
                    <svg class="h-4 w-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                </Button>
                <Button variant="destructive" size="sm" @click="confirmDelete(item)" class="h-8 w-8 p-0 rounded-full bg-red-600/90 hover:bg-red-600 text-white shadow">
                    <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                </Button>
            </div>
        </div>
        
        <CardContent class="p-5 flex-1 space-y-2">
            <div class="flex justify-between items-start">
                <h3 class="text-lg font-bold text-gray-900 line-clamp-1">{{ item.name }}</h3>
                <span class="text-lg font-black text-indigo-600">${{ item.price }}</span>
            </div>
            <p class="text-sm text-gray-500 line-clamp-3">{{ item.description }}</p>
        </CardContent>
      </Card>
    </div>

    <!-- Edit/Add Modal (Custom implementation since Dialog requires accessible context hard to manual impl) -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200 scale-95">
            <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50">
                <h3 class="text-lg font-bold text-gray-900">{{ isEditing ? 'Edit Item' : 'New Menu Item' }}</h3>
                <button @click="closeModal" class="text-gray-400 hover:text-gray-500">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                </button>
            </div>
            <form @submit.prevent="saveItem">
                <div class="p-6 space-y-4">
                    <div class="space-y-2">
                        <label class="text-sm font-medium leading-none text-gray-700">Name</label>
                        <input v-model="formData.name" required class="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 disabled:cursor-not-allowed disabled:opacity-50" placeholder="e.g. Cheese Burger">
                    </div>
                    <div class="space-y-2">
                        <label class="text-sm font-medium leading-none text-gray-700">Price ($)</label>
                        <input v-model="formData.price" type="number" step="0.01" required class="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 disabled:cursor-not-allowed disabled:opacity-50" placeholder="0.00">
                    </div>
                    <div class="space-y-2">
                        <label class="text-sm font-medium leading-none text-gray-700">Description</label>
                        <textarea v-model="formData.description" class="flex min-h-[80px] w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm ring-offset-background placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 disabled:cursor-not-allowed disabled:opacity-50" placeholder="Describe the dish..."></textarea>
                    </div>
                    <div class="space-y-2">
                        <label class="text-sm font-medium leading-none text-gray-700">Image URL</label>
                         <input v-model="formData.imageUrl" class="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 disabled:cursor-not-allowed disabled:opacity-50" placeholder="https://...">
                    </div>
                </div>
                <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex justify-end space-x-2">
                    <Button type="button" variant="ghost" @click="closeModal" class="text-gray-600 hover:text-gray-900">Cancel</Button>
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
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';

const items = ref([]);
const loading = ref(true);
const error = ref(null);

const showModal = ref(false);
const isEditing = ref(false);
const formData = ref({
    id: null,
    name: '',
    price: '',
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

const openModal = (item = null) => {
    isEditing.value = !!item;
    if (item) {
        formData.value = { ...item };
    } else {
        formData.value = { id: null, name: '', price: '', description: '', imageUrl: '' };
    }
    showModal.value = true;
};

const closeModal = () => {
    showModal.value = false;
    formData.value = { id: null, name: '', price: '', description: '', imageUrl: '' };
};

const saveItem = async () => {
    try {
        const token = localStorage.getItem('token');
        const payload = {
            name: formData.value.name,
            price: parseFloat(formData.value.price),
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
