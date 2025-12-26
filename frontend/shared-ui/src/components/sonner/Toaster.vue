<script setup>
import { Toaster as Sonner } from 'vue-sonner'
import { ref, onMounted, onUnmounted, computed } from 'vue'

// Confirmed: Importing styles is required for correct positioning calculations
import 'vue-sonner/style.css'

const props = defineProps({
  position: { type: String, default: "bottom-right" }, // Default fallback
  expand: { type: Boolean, default: false },
  duration: { type: Number, default: 4000 },
  visibleToasts: { type: Number, default: 3 },
  closeButton: { type: Boolean, default: false },
  toastOptions: { type: Object, default: () => ({}) },
  offset: { type: [String, Number], default: null },
  dir: { type: String, default: "auto" },
  hotkey: { type: Array, default: () => ["altKey", "T"] },
  richColors: { type: Boolean, default: false },
  invert: { type: Boolean, default: false },
  theme: { type: String, default: "system" }
})

// Responsive Positioning Logic
// Mobile (< 768px): top-center (Best visibility, safe from bottom nav)
// Desktop (>= 768px): top-right (Standard notification area)
const currentPosition = ref('top-right');

const updatePosition = () => {
    if (window.matchMedia('(max-width: 768px)').matches) {
        currentPosition.value = 'top-center';
    } else {
        currentPosition.value = 'top-right';
    }
};

onMounted(() => {
    updatePosition();
    window.addEventListener('resize', updatePosition);
});

onUnmounted(() => {
    window.removeEventListener('resize', updatePosition);
});
</script>

<template>
  <Sonner
    class="toaster group"
    :position="currentPosition"
    :richColors="richColors"
    :invert="invert"
    :theme="theme"
    :closeButton="closeButton"
    :toast-options="{
      class: 'group toast group-[.toaster]:bg-card group-[.toaster]:text-card-foreground group-[.toaster]:border-border group-[.toaster]:shadow-lg',
      descriptionClass: 'group-[.toast]:text-muted-foreground',
      actionButtonClass: 'group-[.toast]:bg-primary group-[.toast]:text-primary-foreground',
      cancelButtonClass: 'group-[.toast]:bg-muted group-[.toast]:text-muted-foreground',
      ...toastOptions
    }"
  />
</template>
