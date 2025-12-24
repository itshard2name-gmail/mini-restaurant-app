import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

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
        items.value = {};
    };

    const cartItems = computed(() => Object.values(items.value));

    const totalPrice = computed(() => {
        return cartItems.value.reduce((total, item) => total + (item.price * item.quantity), 0);
    });

    const totalQuantity = computed(() => {
        return cartItems.value.reduce((total, item) => total + item.quantity, 0);
    });

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
