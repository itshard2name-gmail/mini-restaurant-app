<script setup>
import { ref, onErrorCaptured } from 'vue';

const error = ref(null);
const info = ref('');

onErrorCaptured((err, instance, inf) => {
  if (err.message) {
      if (err.message.includes("Cannot destructure property 'bum'")) {
          console.warn("Global Error Boundary Suppressed Vue Federation Unmount Error:", err);
          return false;
      }
      if (err.message.includes("nextSibling")) {
          console.warn("Global Error Boundary Suppressed DOM Race Condition:", err);
          return false;
      }
  }
  error.value = err;
  info.value = inf;
  console.error("Global Error Boundary Caught:", err);
  // Return false to stop the error from propagating further
  return false;
});

const reset = () => {
    error.value = null;
    info.value = '';
};
</script>

<template>
  <div v-if="error" class="p-6 bg-red-50 border border-red-200 rounded-lg m-4">
    <h2 class="text-xl font-bold text-red-700 mb-2">Something went wrong</h2>
    <p class="text-red-600 mb-4">The application encountered an unexpected error.</p>
    
    <div class="bg-red-100 p-4 rounded text-sm font-mono text-red-800 overflow-auto mb-4">
        <strong>Error:</strong> {{ error.message }} <br/>
        <span class="text-xs text-red-500 mt-2 block">{{ info }}</span>
    </div>

    <button @click="reset" class="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 transition">
        Try Again
    </button>
  </div>
  <slot v-else></slot>
</template>
