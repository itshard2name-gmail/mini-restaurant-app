import { ref, onMounted, watch } from 'vue';

const isDark = ref(true); // Default to dark for Admin focus

export function useTheme() {

    const initTheme = () => {
        // specific check: if no storage, check system, but prefer dark for admin initial
        const stored = localStorage.getItem('theme');
        if (stored) {
            isDark.value = stored === 'dark';
        } else {
            // Default to dark as per current design direction, unless system explicitly light
            isDark.value = true;
        }
        applyTheme();
    };

    const toggleTheme = () => {
        isDark.value = !isDark.value;
        localStorage.setItem('theme', isDark.value ? 'dark' : 'light');
        applyTheme();
    };

    const applyTheme = () => {
        if (isDark.value) {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }
    };

    onMounted(() => {
        initTheme();
    });

    return {
        isDark,
        toggleTheme
    };
}
