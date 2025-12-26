import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

// Global state (singleton pattern)
const token = ref(localStorage.getItem('token'));
const isAdmin = ref(false);
const diningInfo = ref(null);
const debugMsg = ref('-');

export function useAuth() {
    const router = useRouter();

    const checkRole = () => {
        const rawToken = localStorage.getItem('token');
        token.value = rawToken;

        console.log('useAuth: checkRole called. Token:', rawToken ? 'Present' : 'None');

        if (!rawToken) {
            isAdmin.value = false;
            diningInfo.value = null;
            debugMsg.value = 'No Token';
            return;
        }

        try {
            const payload = JSON.parse(atob(rawToken.split('.')[1]));
            isAdmin.value = payload.roles && payload.roles.includes('ROLE_ADMIN');

            if (!isAdmin.value) {
                let mode = payload.diningMode;
                let table = payload.tableNumber;
                let source = 'Token';

                if (!mode) {
                    try {
                        const localInfo = JSON.parse(localStorage.getItem('diningInfo') || '{}');
                        if (localInfo.mode) {
                            mode = localInfo.mode;
                            table = localInfo.table;
                            source = 'LocalStorage';
                        }
                    } catch (e) { console.error('Error reading diningInfo', e); }
                }

                diningInfo.value = {
                    mode: mode || 'UNKNOWN',
                    table: table
                };
                debugMsg.value = `Src:${source} Mode:${diningInfo.value.mode}`;
            } else {
                diningInfo.value = null;
                debugMsg.value = 'Admin';
            }
        } catch (e) {
            console.error('Error parsing token:', e);
            isAdmin.value = false;
            diningInfo.value = null;
            debugMsg.value = 'ParseError';
        }
    };

    const handleLogout = () => {
        const wasAdmin = isAdmin.value;

        token.value = null;
        localStorage.removeItem('cart');
        localStorage.removeItem('diningInfo');
        localStorage.removeItem('token');
        localStorage.removeItem('roles');

        // Dispatch event for legacy listeners (if any sub-apps listen to it)
        window.dispatchEvent(new Event('auth-change'));

        if (wasAdmin) {
            router.push('/staff/login');
        } else {
            router.push('/menu');
        }
    };

    return {
        token,
        isAdmin,
        diningInfo,
        debugMsg,
        checkRole,
        handleLogout
    };
}
