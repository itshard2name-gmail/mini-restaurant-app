import { chromium } from 'playwright';

(async () => {
    console.log('Starting E2E Test...');
    const browser = await chromium.launch({ headless: false }); // Headless: false to see it
    const page = await browser.newPage();

    try {
        // 1. Login Flow
        console.log('Step 1: Navigate to Login');
        await page.goto('http://localhost:3000/login');

        // Wait for inputs
        await page.waitForSelector('#username');

        console.log('Step 2: Enter Credentials');
        await page.fill('#username', 'customer');
        await page.fill('#password', '123456');

        console.log('Step 3: Submit Login');
        await page.click('button:has-text("Login")');

        // 2. Verify Redirect to Menu
        await page.waitForURL('**/menu');
        console.log('Login successful, redirected to /menu');

        // 3. Add Item to Cart
        // Wait for menu items to load
        await page.waitForSelector('text=Classic Burger');

        console.log('Step 4: Add "Classic Burger" to Cart');
        // Find the card with "Classic Burger" and click its Add to Cart button
        // We use a locator that finds the button inside a card containing the text
        const addBtn = page.locator('div', { hasText: 'Classic Burger' }).locator('button', { hasText: 'Add to Cart' }).first();
        await addBtn.click();
        console.log('Clicked "Add to Cart"');

        // 4. Submit Order
        console.log('Step 5: Checkout');
        // Wait for item to appear in cart (optional, but good practice)
        await page.waitForTimeout(500); // UI animation

        const checkoutBtn = page.locator('button', { hasText: 'Checkout' });
        if (await checkoutBtn.isVisible()) {
            await checkoutBtn.click();
            console.log('Clicked "Checkout"');

            // 5. Verify Success
            console.log('Step 6: Verify Success Message');
            await page.waitForSelector('text=Order submitted successfully!');
            console.log('✅ E2E Test Passed: Verification successful.');
        } else {
            throw new Error('Checkout button not visible/enabled');
        }

    } catch (error) {
        console.error('❌ E2E Test Failed:', error);
        process.exit(1);
    } finally {
        await page.waitForTimeout(2000); // Leave browser open briefly
        await browser.close();
    }
})();
