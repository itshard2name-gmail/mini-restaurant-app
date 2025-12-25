import { defineStore } from 'pinia';
import { ref, computed, watch } from 'vue';

export const useCartStore = defineStore('cart', () => {
    const items = ref({});
    const orderType = ref(null); // 'DINE_IN' or 'TAKEOUT'
    const tableNumber = ref(null); // e.g. "5"

    const addToCart = (menu) => {
        if (items.value[menu.id]) {
            items.value[menu.id].quantity++;
        } else {
            items.value[menu.id] = {
                menuId: menu.id,
                name: menu.name,
                price: menu.price,
                quantity: 1,
                notes: ''
            };
        }
    };

    const minusItem = (menu) => {
        if (items.value[menu.id]) {
            if (items.value[menu.id].quantity > 1) {
                items.value[menu.id].quantity--;
            } else {
                delete items.value[menu.id];
            }
        }
    };

    const updateItemNotes = (menuId, notes) => {
        if (items.value[menuId]) {
            items.value[menuId].notes = notes;
        }
    };

    const removeItem = (menu) => {
        if (items.value[menu.id]) {
            delete items.value[menu.id];
        }
    };

    const clearCart = () => {
        console.log("Cart cleared (items only, mode preserved)");
        items.value = {};
        // Do NOT reset orderType/tableNumber to allow "Add to Order" flow without re-prompting.
    };

    const cartItems = computed(() => Object.values(items.value));

    const totalPrice = computed(() => {
        return cartItems.value.reduce((total, item) => total + (item.price * item.quantity), 0);
    });

    const totalQuantity = computed(() => {
        return cartItems.value.reduce((total, item) => total + item.quantity, 0);
    });

    // --- Persistence Logic ---
    const STORAGE_KEY = 'restaurant_cart_store';

    // Hydrate
    try {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored) {
            const data = JSON.parse(stored);
            items.value = data.items || {};
            orderType.value = data.orderType || null;
            tableNumber.value = data.tableNumber || null;
        }
    } catch (e) {
        console.error("Failed to rehydrate cart", e);
    }

    // Persist
    // Watch deeply for changes
    watch([items, orderType, tableNumber], () => {
        const data = {
            items: items.value,
            orderType: orderType.value,
            tableNumber: tableNumber.value
        };
        localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    }, { deep: true });

    return {
        items,
        addToCart,
        minusItem,
        removeItem,
        clearCart,
        cartItems,
        totalPrice,
        totalQuantity,
        updateItemNotes,
        orderType,
        tableNumber
    };
});
