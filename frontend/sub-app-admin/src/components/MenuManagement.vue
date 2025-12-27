<template>
  <div class="flex h-[calc(100vh-140px)] bg-background">
    <!-- Sidebar (Categories) -->
    <aside class="w-1/4 min-w-[250px] bg-muted/30 border-r border-border flex flex-col">
        <div class="p-4 border-b border-border flex justify-between items-center bg-muted/20">
            <h3 class="font-bold text-lg text-foreground tracking-tight">Categories</h3>
            <Button variant="ghost" size="sm" class="h-8 w-8 p-0 hover:bg-background border border-transparent hover:border-border" @click="openCategoryModal()" title="Add New Category">
                <svg class="w-5 h-5 text-muted-foreground hover:text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
            </Button>
        </div>
        
        <div class="flex-1 overflow-y-auto p-3 space-y-1">
            <button 
                @click="currentCategory = 'All'"
                class="w-full text-left px-4 py-2.5 rounded-md text-sm font-medium transition-colors flex justify-between items-center"
                :class="currentCategory === 'All' ? 'bg-primary text-primary-foreground shadow-sm' : 'text-muted-foreground hover:bg-muted hover:text-foreground'"
            >
                <span>All Items</span>
                <span class="text-xs opacity-70 bg-background/20 px-1.5 py-0.5 rounded-full">{{ allItems.length }}</span>
            </button>
            
            <div 
                v-for="cat in categories" 
                :key="cat.id"
                class="group flex items-center"
            >
                <button 
                    @click="currentCategory = cat.name"
                    class="flex-1 text-left px-4 py-2.5 rounded-md text-sm font-medium transition-colors flex justify-between items-center"
                    :class="currentCategory === cat.name ? 'bg-primary text-primary-foreground shadow-sm' : 'text-muted-foreground hover:bg-muted hover:text-foreground'"
                >
                    <span>{{ cat.name }}</span>
                    <!-- Context Menu Trigger could go here -->
                </button>
                
                <!-- inline actions (visible on hover) -->
                <div class="ml-1 flex opacity-0 group-hover:opacity-100 transition-opacity">
                    <button @click.stop="openCategoryModal(cat)" class="p-1.5 text-muted-foreground hover:text-primary" title="Edit Category">
                        <svg class="w-3.5 h-3.5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg>
                    </button>
                    <button @click.stop="confirmDeleteCategory(cat)" class="p-1.5 text-muted-foreground hover:text-destructive" title="Delete Category">
                        <svg class="w-3.5 h-3.5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                    </button>
                </div>
            </div>
        </div>
        
        <!-- Footer action moved to header -->
    </aside>

    <!-- Main Content (Menu Items) -->
    <main class="flex-1 flex flex-col overflow-hidden bg-background">
        <!-- Toolbar -->
        <div class="p-6 pb-2 flex justify-between items-center">
            <div>
                <h2 class="text-2xl font-bold text-foreground tracking-tight">{{ currentCategory === 'All' ? 'All Menu Items' : currentCategory }}</h2>
                <p class="text-sm text-muted-foreground mt-1">{{ filteredItems.length }} items showing</p>
            </div>
            <Button @click="openMenuModal()" class="bg-indigo-600 hover:bg-indigo-700 text-white shadow-sm">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                Add Item
            </Button>
        </div>

        <!-- Scrollable Grid -->
        <div class="flex-1 overflow-y-auto p-6 pt-4">
             <div v-if="loading && allItems.length === 0" class="flex justify-center py-12">
                <div class="animate-spin rounded-full h-8 w-8 border-2 border-primary border-t-transparent"></div>
            </div>

            <div v-else-if="filteredItems.length === 0" class="text-center py-16 text-muted-foreground bg-muted/20 border border-dashed border-border rounded-lg">
                <p>No items in this category yet.</p>
                <Button variant="link" @click="openMenuModal()" class="mt-2 text-primary">Add your first item</Button>
            </div>

            <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                <Card v-for="item in filteredItems" :key="item.id" class="group flex flex-col overflow-hidden hover:shadow-lg transition-all duration-300 bg-card border-border">
                    <div class="relative h-40 overflow-hidden bg-muted">
                        <img v-if="item.imageUrl" :src="item.imageUrl" class="w-full h-full object-cover transition-transform group-hover:scale-105" @error="$event.target.src='https://placehold.co/400x300?text=No+Image'" />
                        <div class="absolute top-2 right-2 flex space-x-1 opacity-0 group-hover:opacity-100 transition-all duration-200">
                             <button @click.stop="openMenuModal(item)" class="p-1.5 bg-background/80 hover:bg-background rounded-full shadow text-foreground transition-colors border border-border">
                                <svg class="w-4 h-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg>
                             </button>
                             <button @click.stop="confirmDeleteMenu(item)" class="p-1.5 bg-red-100 hover:bg-red-200 rounded-full shadow text-red-600 transition-colors border border-red-200">
                                <svg class="w-4 h-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                             </button>
                        </div>
                         <div class="absolute bottom-0 left-0 right-0 p-2 bg-gradient-to-t from-black/60 to-transparent">
                            <span class="text-white font-bold text-shadow">${{ item.price }}</span>
                         </div>
                    </div>
                    <CardContent class="p-3">
                        <h4 class="font-semibold text-foreground truncate">{{ item.name }}</h4>
                        <p class="text-xs text-muted-foreground line-clamp-2 mt-1">{{ item.description }}</p>
                         <div class="mt-2 flex items-center">
                            <span class="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded bg-muted text-muted-foreground border border-border">{{ item.category?.name || 'Uncategorized' }}</span>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    </main>

    <!-- Menu Item Modal -->
    <div v-if="showMenuModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
        <div class="bg-popover border border-border rounded-xl shadow-2xl w-full max-w-lg overflow-hidden animate-in fade-in zoom-in duration-200">
             <div class="px-6 py-4 border-b border-border flex justify-between items-center">
                <h3 class="text-lg font-bold text-foreground">{{ isEditingItem ? 'Edit Item' : 'New Menu Item' }}</h3>
                <button @click="closeMenuModal" class="text-muted-foreground hover:text-foreground"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg></button>
            </div>
            <form @submit.prevent="saveMenuItem">
                <div class="p-6 space-y-4">
                    <div class="grid grid-cols-2 gap-4">
                        <div class="col-span-2 space-y-2">
                            <label class="text-sm font-medium text-foreground">Name</label>
                            <input v-model="menuForm.name" required class="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring" placeholder="Classic Burger">
                        </div>
                        <div class="space-y-2">
                             <label class="text-sm font-medium text-foreground">Price</label>
                             <input v-model="menuForm.price" type="number" step="0.01" required class="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring" placeholder="0.00">
                        </div>
                        <div class="space-y-2">
                             <label class="text-sm font-medium text-foreground">Category</label>
                             <select v-model="menuForm.categoryId" class="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring">
                                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                             </select>
                        </div>
                    </div>
                     <div class="space-y-2">
                        <label class="text-sm font-medium text-foreground">Description</label>
                        <textarea v-model="menuForm.description" class="flex min-h-[60px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring" placeholder="Item description..."></textarea>
                    </div>
                    <div class="space-y-2">
                        <label class="text-sm font-medium text-foreground">Image URL</label>
                        <div class="flex gap-2">
                            <input v-model="menuForm.imageUrl" class="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring" placeholder="https://...">
                            <Button type="button" variant="outline" size="sm" @click="generateRandomImage" class="shrink-0">Magic Fill</Button>
                        </div>
                    </div>
                </div>
                <div class="px-6 py-4 bg-muted/20 border-t border-border flex justify-end gap-2">
                    <Button type="button" variant="ghost" @click="closeMenuModal">Cancel</Button>
                    <Button type="submit">Save Changes</Button>
                </div>
            </form>
        </div>
    </div>
    
    <!-- Category Modal -->
    <div v-if="showCategoryModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
        <div class="bg-popover border border-border rounded-xl shadow-2xl w-full max-w-sm overflow-hidden animate-in fade-in zoom-in duration-200">
            <div class="px-6 py-4 border-b border-border flex justify-between items-center">
                <h3 class="text-lg font-bold text-foreground">{{ isEditingCategory ? 'Edit Category' : 'New Category' }}</h3>
                <button @click="closeCategoryModal" class="text-muted-foreground hover:text-foreground"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg></button>
            </div>
             <form @submit.prevent="saveCategory">
                <div class="p-6 space-y-4">
                    <div class="space-y-2">
                        <label class="text-sm font-medium text-foreground">Name</label>
                        <input v-model="categoryForm.name" required class="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm" placeholder="e.g. Specials">
                    </div>
                    <div class="space-y-2">
                        <label class="text-sm font-medium text-foreground">Display Order</label>
                        <input v-model="categoryForm.displayOrder" type="number" class="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm" placeholder="10">
                    </div>
                </div>
                 <div class="px-6 py-4 bg-muted/20 border-t border-border flex justify-end gap-2">
                    <Button type="button" variant="ghost" @click="closeCategoryModal">Cancel</Button>
                    <Button type="submit">{{ isEditingCategory ? 'Save Changes' : 'Create' }}</Button>
                </div>
             </form>
        </div>
    </div>

    <!-- Delete Confirmation Dialog -->
    <Dialog v-model:open="showDeleteDialog">
        <DialogContent class="sm:max-w-[425px]">
            <DialogHeader>
                <DialogTitle>Delete {{ deleteType === 'CATEGORY' ? 'Category' : 'Item' }}?</DialogTitle>
                <DialogDescription>
                    This action cannot be undone. 
                    <span v-if="deleteType === 'CATEGORY'">
                        This category contains <strong>{{ itemToDelete?.itemCount || 0 }}</strong> items.
                        <br><br>
                        <span class="text-destructive font-bold">Warning:</span> All items in this category will also be deleted.
                    </span>
                    <span v-else>
                         Are you sure you want to delete <strong>{{ itemToDelete?.name }}</strong>?
                    </span>
                </DialogDescription>
            </DialogHeader>
            <DialogFooter>
                <Button variant="ghost" @click="showDeleteDialog = false">Cancel</Button>
                <Button variant="destructive" @click="executeDeleteCategory">Delete</Button>
            </DialogFooter>
        </DialogContent>
    </Dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { Card, CardContent, Button, Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter, toast } from '@mini-restaurant/ui';

// Data
const categories = ref([]);
const allItems = ref([]);
const currentCategory = ref('All');
const loading = ref(true);

// Modals
const showMenuModal = ref(false);
const showCategoryModal = ref(false);
const isEditingItem = ref(false); // only for menu items
const isEditingCategory = ref(false);

// Forms
const menuForm = ref({ id: null, name: '', price: '', categoryId: '', description: '', imageUrl: '' });
const categoryForm = ref({ name: '', displayOrder: '' });

// Delete Modals
const showDeleteDialog = ref(false);
const itemToDelete = ref(null);
const deleteType = ref('CATEGORY'); // 'CATEGORY' or 'MENU'

import { nextTick } from 'vue';

/* --- Computeds --- */
const filteredItems = computed(() => {
    if (currentCategory.value === 'All') return allItems.value;
    // Map category object to name for filtering
    return allItems.value.filter(i => i.category?.name === currentCategory.value);
});


/* --- API Calls --- */
const fetchData = async () => {
    loading.value = true;
    const token = localStorage.getItem('token');
    const headers = { Authorization: `Bearer ${token}` };
    
    try {
        const [catRes, menuRes] = await Promise.all([
            axios.get('/api/orders/categories', { headers }),
            axios.get('/api/orders/menus', { headers })
        ]);
        categories.value = catRes.data;
        allItems.value = menuRes.data;
    } catch (e) {
        console.error("Failed to load data", e);
    } finally {
        loading.value = false;
    }
};

/* --- Menu Actions --- */
const openMenuModal = (item = null) => {
    isEditingItem.value = !!item;
    if (item) {
        // Map existing item to form, flattening category object to ID
        menuForm.value = { ...item, categoryId: item.category?.id || '' };
    } else {
        // Smart Default: If a specific category is active (not All), find its ID
        const activeCatObj = categories.value.find(c => c.name === currentCategory.value);
        const defaultCatId = activeCatObj ? activeCatObj.id : (categories.value[0]?.id || '');
        
        menuForm.value = { id: null, name: '', price: '', categoryId: defaultCatId, description: '', imageUrl: '' };
    }
    showMenuModal.value = true;
};

const closeMenuModal = () => {
    showMenuModal.value = false;
    menuForm.value = { id: null, name: '', price: '', categoryId: '', description: '', imageUrl: '' };
};

const saveMenuItem = async () => {
   const token = localStorage.getItem('token');
   // Remove 'category' object key if present, ensure categoryId is sent
   const { category, ...rest } = menuForm.value;
   const payload = { ...rest, price: parseFloat(menuForm.value.price) };
   
   try {
       if (isEditingItem.value) {
           await axios.put(`/api/orders/menus/${menuForm.value.id}`, payload, { headers: { Authorization: `Bearer ${token}` } });
       } else {
           await axios.post('/api/orders/menus', payload, { headers: { Authorization: `Bearer ${token}` } });
       }
       await fetchData();
       closeMenuModal();
   } catch (e) {
       toast.error("Failed to save item");
   }
};


const generateRandomImage = () => {
    const k = menuForm.value.name || 'food';
    menuForm.value.imageUrl = `https://image.pollinations.ai/prompt/${encodeURIComponent(k)}`;
};


/* --- Category Actions --- */
const openCategoryModal = (cat = null) => {
    isEditingCategory.value = !!cat;
    if (cat) {
        // Edit Mode
        categoryForm.value = { id: cat.id, name: cat.name, displayOrder: cat.displayOrder };
    } else {
        // Create Mode
        categoryForm.value = { name: '', displayOrder: '' };
    }
    showCategoryModal.value = true;
};

const closeCategoryModal = () => {
     showCategoryModal.value = false;
};

const saveCategory = async () => {
    try {
        const token = localStorage.getItem('token');
        const payload = {
            name: categoryForm.value.name,
            displayOrder: categoryForm.value.displayOrder ? parseInt(categoryForm.value.displayOrder) : null
        };

        // Capture old name before update to check if we need to sync currentCategory view
        let oldName = null;
        if (isEditingCategory.value) {
            const originalCat = categories.value.find(c => c.id === categoryForm.value.id);
            if (originalCat) oldName = originalCat.name;
        }
        
        if (isEditingCategory.value) {
            await axios.put(`/api/orders/categories/${categoryForm.value.id}`, payload, { headers: { Authorization: `Bearer ${token}` } });
            
            // Sync View: If user is viewing the category being renamed, update the view key
            if (oldName && currentCategory.value === oldName) {
                currentCategory.value = payload.name;
            }
        } else {
            await axios.post('/api/orders/categories', payload, { headers: { Authorization: `Bearer ${token}` } });
        }
        
        await fetchData();
        closeCategoryModal();
    } catch(e) { toast.error("Failed to save category"); }
};

const executeDeleteCategory = async () => {
    if (!itemToDelete.value) return;
    try {
        const token = localStorage.getItem('token');
        // Check if deleting category or menu item
        if (deleteType.value === 'CATEGORY') {
            await axios.delete(`/api/orders/categories/${itemToDelete.value.id}`, { headers: { Authorization: `Bearer ${token}` } });
            if(currentCategory.value === itemToDelete.value.name) currentCategory.value = 'All';
        } else {
             await axios.delete(`/api/orders/menus/${itemToDelete.value.id}`, { headers: { Authorization: `Bearer ${token}` } });
        }
        
        await fetchData();
        showDeleteDialog.value = false;
        itemToDelete.value = null;
    } catch(e) { toast.error("Failed to delete"); }
};


const confirmDeleteCategory = async (cat) => {
    console.log('confirmDeleteCategory clicked', cat);
    const count = allItems.value.filter(i => i.category?.name === cat.name).length;
    itemToDelete.value = { ...cat, itemCount: count };
    deleteType.value = 'CATEGORY';
    await nextTick();
    showDeleteDialog.value = true;
};

const confirmDeleteMenu = async (item) => {
    console.log('confirmDeleteMenu clicked', item);
    itemToDelete.value = item;
    deleteType.value = 'MENU';
    await nextTick();
    showDeleteDialog.value = true;
};

onMounted(() => {
    fetchData();
});
</script>
