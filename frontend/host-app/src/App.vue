<script setup>
import ErrorBoundary from './components/ErrorBoundary.vue';
import { onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useAuth } from './composables/useAuth';

// Layouts
import CustomerLayout from './layouts/CustomerLayout.vue';
import AdminLayout from './layouts/AdminLayout.vue';
import AuthLayout from './layouts/AuthLayout.vue';

const route = useRoute();
const { checkRole } = useAuth();

// Layout Mapping
const layoutMap = {
  CustomerLayout,
  AdminLayout,
  AuthLayout
};

// Dynamic Layout Resolution
const currentLayout = computed(() => {
  const layoutName = route.meta.layout;
  return layoutMap[layoutName] || CustomerLayout; // Default to CustomerLayout
});

// App Initialization Logic
onMounted(() => {
  checkRole();
  window.addEventListener('auth-change', checkRole);
});

// Watch route as backup for role checks
watch(() => route.path, () => {
    checkRole();
});
</script>

<template>
  <component :is="currentLayout">
    <ErrorBoundary>
      <router-view :key="$route.fullPath"></router-view>
    </ErrorBoundary>
  </component>
</template>
