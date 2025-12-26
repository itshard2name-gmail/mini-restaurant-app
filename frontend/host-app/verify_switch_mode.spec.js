
import { test, expect } from '@playwright/test';

test('Guest Switch Dining Mode Flow', async ({ page }) => {
    // Capture console logs and errors from the START
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));
    page.on('pageerror', err => console.log('PAGE ERROR:', err.message));
    page.on('requestfailed', request => console.log('REQUEST FAILED:', request.url(), request.failure().errorText));

    // Go to Login Page
    await page.goto('http://localhost:3000/login');

    // 1. Verify Login UI has Dining Mode Selection
    await expect(page.getByText('Accomo (Dine-in)')).toBeVisible();
    await expect(page.getByText('Takeout')).toBeVisible();

    // Take screenshot of Login Page
    await page.screenshot({ path: '1_login_page_with_modes.png' });

    // 2. Login as Guest (Dine-in Table 5)
    // Select Dine-in (Default, but good to click)
    await page.getByText('Accomo (Dine-in)').click();

    // Enter Table Number
    await page.fill('input[type="number"]', '5');

    // Enter Phone
    await page.fill('input[type="tel"]', '0912345678');

    // Click Start Ordering
    await page.click('button:has-text("Start Ordering")');

    // Wait for navigation to Menu
    try {
        await page.waitForURL('**/menu', { timeout: 10000 });
    } catch (e) {
        console.log('Navigation failed! Taking error screenshot.');
        await page.screenshot({ path: 'error_state.png' });
        throw e;
    }

    // 3. Verify Navbar UI
    // Check for "Dine-in #5" badge
    await expect(page.locator('nav')).toContainText('Dine-in #5');

    // Check for "Switch Mode" button
    const switchBtn = page.getByRole('button', { name: 'Switch Mode' });
    await expect(switchBtn).toBeVisible();

    // Take screenshot of Menu Page with Navbar
    await page.screenshot({ path: '2_menu_page_with_switch_mode.png' });

    // 4. Test Switch Mode
    await switchBtn.click();

    // Verify redirection back to Login
    await page.waitForURL('**/login');
    await expect(page.getByText('Welcome')).toBeVisible();

    // Take screenshot of Return to Login
    await page.screenshot({ path: '3_back_to_login.png' });

    console.log('Switch Dining Mode verification passed!');
});
