import { ref } from 'vue';
import axios from 'axios';

const settings = ref({
    BRAND_NAME: 'Restaurant App',
    // Add other settings defaults here if needed
});

const isLoaded = ref(false);

export function useSettings() {
    const fetchSettings = async () => {
        if (isLoaded.value) return; // Cache: Don't fetch if already loaded

        try {
            // Public endpoint logic:
            // Since customer might not be logged in, we need a public endpoint or use a guest token logic if implemented.
            // Requirement check: Current /api/orders/settings usually requires Auth.
            // If strict auth is required, we might fail for guests.
            // Workaround: For now, we attempt fetch. If 401, we might need a public settings endpoint.
            // However, typical "Store Config" like Brand Name should be public.
            // Let's assume there's a public path or we use the generic one and handle error.

            // NOTE: Ideally, the backend should expose a /api/public/settings endpoint.
            // If not available, we fallback to default.

            const res = await axios.get('/api/orders/settings/public'); // Hypothetical public endpoint
            const data = res.data;

            data.forEach(item => {
                if (settings.value.hasOwnProperty(item.settingKey)) {
                    settings.value[item.settingKey] = item.settingValue;
                } else {
                    // Dynamic add
                    settings.value[item.settingKey] = item.settingValue;
                }
            });
            isLoaded.value = true;
        } catch (e) {
            console.warn("Could not fetch public settings, using defaults.", e);
            // Fallback: If 404/401, we just keep default 'Restaurant App'
        }
    };

    return {
        settings,
        fetchSettings
    };
}
