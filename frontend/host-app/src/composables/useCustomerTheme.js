import { ref } from 'vue';
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

    const clearTheme = () => {
        const root = document.documentElement;
        // We need to clear all possible theme variables. 
        // Since we don't know exactly which theme was active if we just use 'currentTheme',
        // it's safer to iterate over the keys of the current theme or a known list.
        // For simplicity, let's clear keys from the current theme logic.

        const theme = themes[currentTheme.value];
        if (theme) {
            Object.keys(theme.colors).forEach((key) => {
                root.style.removeProperty(key);
            });
        }
    };

    return {
        currentTheme,
        setTheme,
        initTheme,
        clearTheme, // Exported
        themes
    };
}
