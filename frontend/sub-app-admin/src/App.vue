<script setup>
import { onMounted, ref } from 'vue';
import AdminDashboard from './components/AdminDashboard.vue';

const isAuthenticated = ref(false);

onMounted(() => {
  const token = localStorage.getItem('token');
  const userRoles = JSON.parse(localStorage.getItem('roles') || '[]');
  const isAdmin = userRoles.includes('ROLE_ADMIN');

  if (!token || !isAdmin) {
    // Redirect to Host App Login
    // We assume Host App is at port 10000 based on standard dev config
    window.location.href = 'http://localhost:10000/staff/login';
  } else {
    isAuthenticated.value = true;
  }
});
</script>

<template>
  <AdminDashboard v-if="isAuthenticated" />
  <div v-else class="flex items-center justify-center min-h-screen">
    <p>Redirecting to login...</p>
  </div>
</template>

<style>
/* Global resets for standalone mode if necessary */
body {
  margin: 0;
  padding: 0;
}
</style>
