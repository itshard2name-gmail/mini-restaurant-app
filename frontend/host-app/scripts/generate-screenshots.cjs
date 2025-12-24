
const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

const TARGET_URL = 'http://localhost:3000';
const OUTPUT_DIR = path.resolve(__dirname, '../../../docs/manual-screenshots');
const ADMIN_USER = 'admin';
const ADMIN_PASS = '123456';

(async () => {
    if (!fs.existsSync(OUTPUT_DIR)) {
        fs.mkdirSync(OUTPUT_DIR, { recursive: true });
    }

    const browser = await chromium.launch({ headless: false });

    // Mobile Context for Dine-In
    const contextMobile = await browser.newContext({
        viewport: { width: 390, height: 844 },
        userAgent: 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1'
    });

    // Desktop Context for Admin
    const contextDesktop = await browser.newContext({
        viewport: { width: 1280, height: 800 }
    });

    console.log('Starting Part 2 Automation...');

    try {
        // ==========================================
        // SCENARIO 2: CUSTOMER DINE-IN (Mobile)
        // ==========================================
        console.log('--- Running Scenario 2: Customer Dine-in ---');
        const pageDine = await contextMobile.newPage();

        // 1. Visit Home
        await pageDine.goto(TARGET_URL);

        // 2. Select Dine-in
        // Wait for dialog. If we have existing session it might not show? 
        // Incognito context should be fresh.
        await pageDine.waitForSelector('text=Dine In', { timeout: 10000 }).catch(e => console.log('Dine In button not found: ' + e));

        if (await pageDine.isVisible('text=Dine In')) {
            await pageDine.click('text=Dine In');
            await pageDine.waitForTimeout(500);

            // 3. Select Table
            await pageDine.click('text=1', { exact: true }).catch(() => { });
        }

        // 4. Capture Menu (Dine-in mode)
        await pageDine.waitForTimeout(2000);
        await pageDine.screenshot({ path: path.join(OUTPUT_DIR, 'dine_in_menu.png') });
        console.log('Captured: dine_in_menu.png');

        // 5. Build Cart
        const addBtns2 = await pageDine.$$('button:has-text("Add")');
        if (addBtns2.length > 0) {
            await addBtns2[0].click();
            await pageDine.waitForTimeout(500);
            if (addBtns2.length > 1) await addBtns2[1].click();
        }

        // 6. Capture Cart
        // Mobile floating button
        const cartBtnSelector = 'button.h-16.w-16.rounded-full';
        await pageDine.waitForSelector(cartBtnSelector, { state: 'visible', timeout: 5000 });
        await pageDine.click(cartBtnSelector);

        await pageDine.waitForTimeout(1000);
        await pageDine.screenshot({ path: path.join(OUTPUT_DIR, 'dine_in_cart.png') });
        console.log('Captured: dine_in_cart.png');

        await pageDine.close();


        // ==========================================
        // SCENARIO 3: ADMIN OPERATIONS (Desktop)
        // ==========================================
        console.log('--- Running Scenario 3: Admin Operations ---');
        const pageAdmin = await contextDesktop.newPage();

        // 1. Visit Login
        await pageAdmin.goto(`${TARGET_URL}/login`);

        // 2. Login as Admin
        await pageAdmin.click('text=Staff');
        await pageAdmin.fill('input#username', ADMIN_USER);
        await pageAdmin.fill('input#password', ADMIN_PASS);
        await pageAdmin.click('button:has-text("Login")');

        // 3. Admin Dashboard
        await pageAdmin.waitForURL('**/admin');
        await pageAdmin.waitForTimeout(3000); // Wait for data load
        await pageAdmin.screenshot({ path: path.join(OUTPUT_DIR, 'admin_dashboard_active.png') });
        console.log('Captured: admin_dashboard_active.png');

        // 4. Tabs: Kitchen
        // Look for tab with text "Kitchen"
        await pageAdmin.click('button:has-text("Kitchen")');
        await pageAdmin.waitForTimeout(1000);
        await pageAdmin.screenshot({ path: path.join(OUTPUT_DIR, 'admin_kitchen.png') });
        console.log('Captured: admin_kitchen.png');

        // 5. Tabs: Counter
        await pageAdmin.click('button:has-text("Counter")');
        await pageAdmin.waitForTimeout(1000);
        await pageAdmin.screenshot({ path: path.join(OUTPUT_DIR, 'admin_counter.png') });
        console.log('Captured: admin_counter.png');

        await pageAdmin.close();

    } catch (error) {
        console.error('Error during automation:', error);
    } finally {
        await browser.close();
        console.log('Automation Complete.');
    }
})();
