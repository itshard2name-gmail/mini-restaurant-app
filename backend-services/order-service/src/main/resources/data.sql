INSERT IGNORE INTO restaurant_settings (setting_key, setting_value) VALUES ('TIMEZONE', 'Asia/Taipei');
INSERT IGNORE INTO restaurant_settings (setting_key, setting_value) VALUES ('TABLE_LIST', '1,2,3,4,5,6,7,8,9,10,VIP1');
INSERT IGNORE INTO restaurant_settings (setting_key, setting_value) VALUES ('BRAND_NAME', 'Antigravity Bistro');

-- Categories
INSERT IGNORE INTO category (id, name, display_order) VALUES (1, 'Main', 1);
INSERT IGNORE INTO category (id, name, display_order) VALUES (2, 'Starters', 2);
INSERT IGNORE INTO category (id, name, display_order) VALUES (3, 'Drink', 3);
INSERT IGNORE INTO category (id, name, display_order) VALUES (4, 'Dessert', 4);

-- Menu Items
INSERT IGNORE INTO menu (id, name, price, description, image_url, category_id) VALUES (1, 'Classic Burger', 12.99, 'Juicy beef patty with lettuce, tomato, and cheese', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=500&q=60', 1);
INSERT IGNORE INTO menu (id, name, price, description, image_url, category_id) VALUES (2, 'Margherita Pizza', 15.50, 'Traditional Italian pizza with basil and mozzarella', 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?auto=format&fit=crop&w=500&q=60', 1);
INSERT IGNORE INTO menu (id, name, price, description, image_url, category_id) VALUES (3, 'Caesar Salad', 8.99, 'Crisp romaine lettuce', 'https://images.unsplash.com/photo-1550304943-4f24f54ddde9?w=800', 2);
INSERT IGNORE INTO menu (id, name, price, description, image_url, category_id) VALUES (4, 'Spaghetti Carbonara', 14.00, 'Creamy pasta with pancetta and black pepper', 'https://images.unsplash.com/photo-1612874742237-6526221588e3?auto=format&fit=crop&w=500&q=60', 1);
