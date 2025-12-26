import { ref, onMounted } from 'vue';
import { themes } from '../lib/themes';

const currentTheme = ref('bistro'); // Default to Bistro as it's neutral

export function useCustomerTheme() {

    const setTheme = (themeId) => {
        const theme = themes[themeId];
        if (!theme) return;

        currentTheme.value = themeId;

        // Apply Variables to :root
        // using document.documentElement.style.setProperty
        const root = document.documentElement;

        Object.entries(theme.colors).forEach(([key, value]) => {
            root.style.setProperty(key, value);
        });

        // Persist
        localStorage.setItem('customer-theme', themeId);
    };

    const initTheme = () => {
        const saved = localStorage.getItem('customer-theme') || 'bistro';
        setTheme(saved);
    };

    return {
        currentTheme,
        setTheme,
        initTheme,
        themes
    };
}
