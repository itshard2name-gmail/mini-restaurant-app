/** @type {import('tailwindcss').Config} */
export default {
	darkMode: ["class"],
	darkMode: ["class"],
	important: '#admin-wrapper', // Scope all styles to this ID
	content: [
		"./index.html",
		"./src/**/*.{vue,js,ts,jsx,tsx}",
	],
	// Safelist dynamic classes (patterns are safer)
	// Safelist removed - using shared UI
	safelist: [],
	// Disable preflight to avoid global style pollution in micro-frontend
	corePlugins: {
		preflight: false,
	},
	theme: {
		extend: {
			borderRadius: {
				lg: 'var(--admin-radius)',
				md: 'calc(var(--admin-radius) - 2px)',
				sm: 'calc(var(--admin-radius) - 4px)'
			},
			colors: {
				background: 'hsl(var(--admin-background))',
				foreground: 'hsl(var(--admin-foreground))',
				card: {
					DEFAULT: 'hsl(var(--admin-card))',
					foreground: 'hsl(var(--admin-card-foreground))'
				},
				popover: {
					DEFAULT: 'hsl(var(--admin-popover))',
					foreground: 'hsl(var(--admin-popover-foreground))'
				},
				primary: {
					DEFAULT: 'hsl(var(--admin-primary))',
					foreground: 'hsl(var(--admin-primary-foreground))'
				},
				secondary: {
					DEFAULT: 'hsl(var(--admin-secondary))',
					foreground: 'hsl(var(--admin-secondary-foreground))'
				},
				muted: {
					DEFAULT: 'hsl(var(--admin-muted))',
					foreground: 'hsl(var(--admin-muted-foreground))'
				},
				accent: {
					DEFAULT: 'hsl(var(--admin-accent))',
					foreground: 'hsl(var(--admin-accent-foreground))'
				},
				destructive: {
					DEFAULT: 'hsl(var(--admin-destructive))',
					foreground: 'hsl(var(--admin-destructive-foreground))'
				},
				border: 'hsl(var(--admin-border))',
				input: 'hsl(var(--admin-input))',
				ring: 'hsl(var(--admin-ring))',
				chart: {
					'1': 'hsl(var(--admin-chart-1))',
					'2': 'hsl(var(--admin-chart-2))',
					'3': 'hsl(var(--admin-chart-3))',
					'4': 'hsl(var(--admin-chart-4))',
					'5': 'hsl(var(--admin-chart-5))'
				}
			}
		}
	},
	plugins: [require("tailwindcss-animate")],
}
