export const themes = {
    bar: {
        id: 'bar',
        name: 'Bar Night',
        colors: {
            '--background': '270 50% 5%',      // Zinc-950 equivalent (Dark Indigo/Black)
            '--foreground': '0 0% 98%',
            '--card': '270 50% 8%',            // Slightly lighter dark
            '--card-foreground': '0 0% 98%',
            '--popover': '270 50% 5%',
            '--popover-foreground': '0 0% 98%',
            '--primary': '280 75% 60%',        // Neon Purple
            '--primary-foreground': '0 0% 100%',
            '--secondary': '270 30% 15%',
            '--secondary-foreground': '0 0% 98%',
            '--muted': '270 30% 15%',
            '--muted-foreground': '270 20% 65%',
            '--accent': '270 50% 15%',
            '--accent-foreground': '0 0% 98%',
            '--destructive': '0 62.8% 30.6%',
            '--destructive-foreground': '0 0% 98%',
            '--border': '270 30% 20%',
            '--input': '270 30% 20%',
            '--ring': '280 75% 60%',
            '--radius': '0rem', // Sharp
        }
    },
    bistro: {
        id: 'bistro',
        name: 'Bistro Dining',
        colors: {
            '--background': '40 20% 97%',      // Warm Stone/Paper (Stone-100ish)
            '--foreground': '24 10% 10%',      // Dark Brown/Gray
            '--card': '0 0% 100%',             // White
            '--card-foreground': '24 10% 10%',
            '--popover': '0 0% 100%',
            '--popover-foreground': '24 10% 10%',
            '--primary': '20 80% 50%',         // Rust Orange
            '--primary-foreground': '0 0% 100%',
            '--secondary': '40 20% 92%',
            '--secondary-foreground': '24 10% 10%',
            '--muted': '40 20% 92%',
            '--muted-foreground': '24 5% 45%',
            '--accent': '40 20% 90%',
            '--accent-foreground': '24 10% 10%',
            '--destructive': '0 84.2% 60.2%',
            '--destructive-foreground': '0 0% 98%',
            '--border': '40 10% 85%',
            '--input': '40 10% 85%',
            '--ring': '20 80% 50%',
            '--radius': '0.5rem', // Soft Round
        }
    },
    cafe: {
        id: 'cafe',
        name: 'Hipster Cafe', // Reverted per user request
        colors: {
            '--background': '40 25% 97%',     // Warm Rice Paper / Linen
            '--foreground': '30 10% 20%',     // Soft Charcoal/Ink
            '--card': '0 0% 100%',            // Pure White (Clean separation)
            '--card-foreground': '30 10% 20%',
            '--popover': '0 0% 100%',
            '--popover-foreground': '30 10% 20%',
            '--primary': '35 25% 50%',        // Muted Wood / Aged Oak
            '--primary-foreground': '0 0% 100%',
            '--secondary': '40 20% 92%',      // Light Beige secondary
            '--secondary-foreground': '30 10% 20%',
            '--muted': '40 20% 94%',
            '--muted-foreground': '30 5% 50%',
            '--accent': '40 20% 92%',
            '--accent-foreground': '30 10% 20%',
            '--destructive': '0 60% 50%',
            '--destructive-foreground': '0 0% 98%',
            '--border': '40 15% 90%',         // Soft border
            '--input': '40 15% 90%',
            '--ring': '35 25% 50%',
            '--radius': '0.3rem',             // Clean, minimal corners (not too round)
        }
    }
};
