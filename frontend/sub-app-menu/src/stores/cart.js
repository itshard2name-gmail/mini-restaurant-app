import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useCartStore = defineStore('cart', () => {
    const items = ref({});

    const addToCart = (menu) => {
        if (items.value[menu.id]) {
            items.value[menu.id].quantity++;
        } else {
            items.value[menu.id] = {
                menuId: menu.id,
                name: menu.name,
                price: menu.price,
                quantity: 1
            };
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
        clearCart,
        cartItems,
        totalPrice,
        totalQuantity
    };
});
