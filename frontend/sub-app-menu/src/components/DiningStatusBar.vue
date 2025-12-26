<script setup>
import { computed } from 'vue';

const props = defineProps({
    diningInfo: {
        type: Object,
        default: null
    }
});

const emit = defineEmits(['change-mode']);

const modeTableText = computed(() => {
    if (!props.diningInfo) return '';
    if (props.diningInfo.mode === 'DINE_IN') {
        return `🍽️  Dining In • Table ${props.diningInfo.table}`;
    }
    return '🛍️  Takeout';
});
</script>

<template>
    <div v-if="diningInfo" class="bg-white border-b border-gray-200 sticky top-0 z-40 shadow-sm transition-all duration-300">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-14 flex items-center justify-between">
            <div class="flex items-center gap-2 text-gray-800 font-bold text-lg">
                <span>{{ modeTableText }}</span>
            </div>
            <button 
                @click="$emit('change-mode')"
                class="px-4 py-1.5 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 hover:text-orange-600 hover:border-orange-200 transition-colors"
            >
                Change Mode
            </button>
        </div>
    </div>
</template>
