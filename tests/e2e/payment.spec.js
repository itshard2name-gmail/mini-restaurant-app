import { test, expect } from '@playwright/test';

test.describe('Payment Flow Verification', () => {

    test('Dine-In Flow: Pay Later', async ({ page }) => {
        // 1. Navigate to Home
        await page.goto('http://localhost:10000');
        await page.waitForLoadState('networkidle');

        // 2. Select Dining Mode: Dine In
        await expect(page.getByText('Dine In')).toBeVisible({ timeout: 10000 });
        await page.getByText('Dine In').click();

        // Select Table 1
        await expect(page.getByText('1', { exact: true })).toBeVisible();
        await page.getByText('1', { exact: true }).click();

        // 3. Add Item to Cart
        await page.waitForSelector('text=Add to Cart', { timeout: 10000 });
        await page.locator('text=Add to Cart').first().click();

        // 4. Verify Payment Method is HIDDEN in Cart (Dine-In specific)
        const creditCardBtn = page.locator('button').filter({ hasText: 'Credit Card' });
        await expect(creditCardBtn).not.toBeVisible();

        // 5. Submit Order (Place Order Only)
        await page.click('text=Checkout Now');

        // 6. Verify Redirect to My Orders (NOT Gateway)
        await expect(page).toHaveURL(/.*\/my-orders/);

        // 7. Click "Pay Now" in My Orders
        await page.getByRole('button', { name: 'Pay Now' }).first().click();

        // 8. Verify Payment Modal Appears
        await expect(page.getByText('Select Payment Method')).toBeVisible();
        await expect(page.getByText('Credit Card')).toBeVisible();

        // 9. Select Credit Card and Confirm
        const ccButton = page.getByRole('button').filter({ hasText: 'Credit Card' });
        await ccButton.click();
        await expect(ccButton).toHaveClass(/border-primary/); // Ensure selection is registered
        await page.getByText('Confirm & Pay').click();

        // 10. Verify Redirect to Mock Gateway
        await expect(page).toHaveURL(/.*\/orders\/payment\/mock-page.*/, { timeout: 10000 });

        // 11. Complete Payment
        await page.click('text=Pay Now'); // Button on Mock Gateway

        // 12. Verify Final Success
        await expect(page).toHaveURL(/.*\/my-orders/);
        // Use text locator instead of class .badge which might not exist
        await expect(page.getByText('PAID', { exact: true }).first()).toBeVisible();
    });

    test('Takeout Flow: Pay Now', async ({ page }) => {
        // 1. Login first (Takeout requires login)
        await page.goto('http://localhost:10000/login');
        await page.fill('input[type="text"]', 'user');
        await page.fill('input[type="password"]', 'user'); // Assuming user/user credentials exist
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL(/.*\/menu/);

        // 2. Select Dining Mode: Takeout
        // Need to change mode if it defaulted to something else or clean state
        // Click on status bar to change mode if visible, or restart flow
        // The /menu page might auto-prompt if no mode selected, but we logged in. 
        // Let's force mode via URL param or click Change Mode

        // If "Table ?" or "Dine In" is shown in status bar, click it to change
        const changeModeBtn = page.getByRole('button', { name: /Table|Takeout|Dine In/ });
        if (await changeModeBtn.isVisible()) {
            await changeModeBtn.click();
        }

        await expect(page.getByText('Take Out')).toBeVisible();
        await page.getByText('Take Out').click();

        // 3. Add Item to Cart
        await page.waitForSelector('text=Add to Cart', { timeout: 10000 });
        await page.locator('text=Add to Cart').first().click();

        // 4. Verify Payment Method is VISIBLE in Cart (Takeout specific)
        const creditCardBtn = page.locator('button').filter({ hasText: 'Credit Card' });
        await expect(creditCardBtn).toBeVisible();

        // Select Credit Card
        await creditCardBtn.click();
        await expect(creditCardBtn).toHaveClass(/border-primary/);

        // 5. Submit Order (Pay Now)
        await page.click('text=Checkout Now');

        // 6. Verify Immediate Redirect to Gateway
        await expect(page).toHaveURL(/.*\/orders\/payment\/mock-page.*/, { timeout: 10000 });

        // 7. Complete Payment
        await page.click('text=Pay Now');

        // 8. Verify Final Success
        await expect(page).toHaveURL(/.*\/my-orders/);
        await expect(page.getByText('PAID', { exact: true }).first()).toBeVisible();
    });

});
