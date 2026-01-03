import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import StaffLogin from '../components/StaffLogin.vue'
// Basic error boundary for remote components
const MenuList = () =>
    import('sub-app-menu/MenuPage').catch(err => {
        console.error("Failed to load MenuPage remote:", err);
        return { template: '<div class="text-red-500">Failed to load Menu module. Ensure sub-app is running.</div>' }
    })

const MyOrders = () =>
    import('sub-app-menu/MyOrders').catch(err => {
        console.error("Failed to load MyOrders remote:", err);
        return { template: '<div class="text-red-500">Failed to load Order module.</div>' }
    })

const AdminDashboard = () =>
    import('sub-app-admin/AdminDashboard').catch(err => {
        console.error("Failed to load AdminDashboard remote:", err);
        return { template: '<div class="text-red-500">Failed to load Admin module.</div>' }
    })

const routes = [
    { path: '/', redirect: '/menu' },
    { path: '/login', component: Login, meta: { guest: true, layout: 'AuthLayout' } },
    { path: '/staff/login', component: StaffLogin, meta: { guest: true, layout: 'AuthLayout' } },
    { path: '/menu', component: MenuList, meta: { requiresAuth: false, layout: 'CustomerLayout' } },
    { path: '/my-orders', component: MyOrders, meta: { requiresAuth: false, layout: 'CustomerLayout' } }, // Allowed for Guest Tokens too
    { path: '/admin', component: AdminDashboard, meta: { requiresAuth: true, roles: ['ROLE_ADMIN'], layout: 'AdminLayout' } }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const customerToken = localStorage.getItem('token');
    const customerRoles = JSON.parse(localStorage.getItem('roles') || '[]');

    const adminToken = localStorage.getItem('admin_token');
    const adminRoles = JSON.parse(localStorage.getItem('admin_roles') || '[]');

    const isAdminRoute = to.path.startsWith('/admin') || (to.meta.roles && to.meta.roles.includes('ROLE_ADMIN'));

    // --- ADMIN ROUTES GUARD ---
    if (isAdminRoute) {
        if (!adminToken) {
            return next('/staff/login');
        }

        // Verify Admin Roles
        if (to.meta.roles && to.meta.roles.length > 0) {
            const hasRole = to.meta.roles.some(role => adminRoles.includes(role));
            if (!hasRole) {
                // Have admin token but incorrect role? Force re-login or deny
                return next('/staff/login');
            }
        }
        return next(); // Proceed to Admin Page
    }

    // --- CUSTOMER / SHARED ROUTES GUARD ---
    if (to.meta.requiresAuth) {
        // This block handles Customer routes that strictly require login (none currently in this list, but good for future)
        // Note: /menu and /my-orders are requiresAuth: false
        if (!customerToken) {
            return next('/login');
        }

        // Check Customer Roles if needed
        if (to.meta.roles && to.meta.roles.length > 0) {
            const hasRole = to.meta.roles.some(role => customerRoles.includes(role));
            if (!hasRole) return next('/menu');
        }
    } else if (to.meta.guest) {
        // Prevent logged-in users from visiting login pages unexpectedly
        // Staff Login Limit
        if (to.path === '/staff/login' && adminToken) {
            return next('/admin');
        }

        // Customer Login Limit
        if (to.path === '/login' && customerToken) {
            // Exception: Allow if explicity switching mode
            if (to.query.mode === 'TAKEOUT') return next();
            return next('/menu');
        }
    }

    // Allow access to public routes
    next();
});

export default router
