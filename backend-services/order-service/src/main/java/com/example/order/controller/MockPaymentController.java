package com.example.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders/payment")
public class MockPaymentController {

    @GetMapping("/mock-page")
    public ResponseEntity<String> getMockPaymentPage(@RequestParam Long orderId, @RequestParam String amount) {
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Mock Payment Gateway</title>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f4f4f9; }
                        .card { background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); width: 100%%; max-width: 400px; }
                        .banner { background-color: #ef4444; color: white; text-align: center; padding: 0.5rem; border-radius: 6px; margin-bottom: 1.5rem; font-weight: bold; font-size: 0.9rem; }
                        h2 { margin-top: 0; color: #1f2937; }
                        .amount { font-size: 2rem; font-weight: bold; color: #10b981; margin: 1rem 0; }
                        label { display: block; margin-bottom: 0.5rem; color: #4b5563; }
                        input { width: 100%%; padding: 0.75rem; margin-bottom: 1rem; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box; }
                        button { width: 100%%; padding: 0.75rem; background-color: #3b82f6; color: white; border: none; border-radius: 6px; font-weight: bold; cursor: pointer; transition: background 0.2s; }
                        button:hover { background-color: #2563eb; }
                        .note { font-size: 0.8rem; color: #6b7280; margin-top: 1rem; text-align: center; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="banner">⚠️ DEMO ENVIRONMENT - DO NOT USE REAL CARDS</div>
                        <h2>Secure Payment</h2>
                        <p>Order ID: <strong>%s</strong></p>
                        <div class="amount">$%s</div>

                        <form action="/api/orders/payment/callback" method="POST">
                            <input type="hidden" name="orderId" value="%s">

                            <label>Card Number</label>
                            <input type="text" placeholder="4242 4242 4242 4242" value="4242 4242 4242 4242">

                            <div style="display: flex; gap: 1rem;">
                                <div style="flex: 1;">
                                    <label>Expiry</label>
                                    <input type="text" placeholder="MM/YY" value="12/30">
                                </div>
                                <div style="flex: 1;">
                                    <label>CVC</label>
                                    <input type="text" placeholder="123" value="123">
                                </div>
                            </div>

                            <div style="display: flex; gap: 1rem; margin-top: 1rem;">
                                <a href="http://localhost:10000/my-orders" style="flex: 1; display: block; text-align: center; padding: 0.75rem; background-color: #9ca3af; color: white; border-radius: 6px; text-decoration: none; font-weight: bold; transition: background 0.2s;">Cancel</a>
                                <button type="submit" style="flex: 1; margin-top: 0;">Pay Now</button>
                            </div>
                        </form>
                        <div class="note">This is a simulated payment page for demonstration purposes.</div>
                    </div>
                </body>
                </html>
                """
                .formatted(orderId, amount, orderId);

        return ResponseEntity.ok(html);
    }
}
