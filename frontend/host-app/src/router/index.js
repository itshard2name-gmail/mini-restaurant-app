import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import { defineAsyncComponent } from 'vue'

// Basic error boundary for remote components
const MenuList = defineAsyncComponent(() =>
    import('sub-app-menu/MenuPage').catch(err => {
        console.error("Failed to load MenuPage remote:", err);
        return { template: '<div class="text-red-500">Failed to load Menu module. Ensure sub-app is running.</div>' }
    })
)

const MyOrders = defineAsyncComponent(() =>
    import('sub-app-menu/MyOrders').catch(err => {
        console.error("Failed to load MyOrders remote:", err);
        return { template: '<div class="text-red-500">Failed to load Order module.</div>' }
    })
)

const AdminDashboard = defineAsyncComponent(() =>
    import('sub-app-admin/AdminDashboard').catch(err => {
        console.error("Failed to load AdminDashboard remote:", err);
        return { template: '<div class="text-red-500">Failed to load Admin module.</div>' }
    })
)

const routes = [
    { path: '/', redirect: '/menu' },
    { path: '/login', component: Login, meta: { guest: true } },
    { path: '/menu', component: MenuList, meta: { requiresAuth: false } },
    { path: '/my-orders', component: MyOrders, meta: { requiresAuth: true, roles: ['ROLE_USER', 'ROLE_ADMIN'] } },
    { path: '/admin', component: AdminDashboard, meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] } }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token');
    const userRoles = JSON.parse(localStorage.getItem('roles') || '[]');

    const toRoles = to.meta.roles;

    // Check if route requires auth
    if (to.meta.requiresAuth) {
        if (!token) {
            return next('/login');
        }

        // Check for specific role requirements
        if (toRoles && toRoles.length > 0) {
            const hasRole = toRoles.some(role => userRoles.includes(role));

            if (!hasRole) {
                // Strict redirect logic
                if (to.path.startsWith('/admin') && userRoles.includes('ROLE_USER')) {
                    alert('Access Denied: You do not have permission to view the Admin Dashboard.');
                    return next('/menu');
                }

                // General fallback
                if (userRoles.includes('ROLE_ADMIN')) return next('/admin');
                if (userRoles.includes('ROLE_USER')) return next('/menu');
                return next('/login');
            }
        }
    } else if (to.meta.guest && token) {
        // Redirect logic if already logged in (e.g. trying to access login page)
        if (userRoles.includes('ROLE_ADMIN')) return next('/admin');
        return next('/menu');
    }

    // Allow access to public routes (like /menu)
    next();
});

export default router
