<script setup>
import { onMounted, onUnmounted } from 'vue';
import { useCustomerTheme } from '../composables/useCustomerTheme';

const { initTheme, clearTheme } = useCustomerTheme();

onMounted(() => {
    initTheme();
});

onUnmounted(() => {
    clearTheme();
});
</script>

<template>
  <div class="bg-[hsl(var(--background))] text-[hsl(var(--foreground))] min-h-screen transition-colors duration-300">
       <slot />
  </div>
</template>
