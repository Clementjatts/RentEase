-- RentEase Test Data

-- Insert test users (passwords are hashed versions of 'password' and 'admin123')
INSERT INTO users (id, username, email, password, role) VALUES
(1, 'testuser', 'user@example.com', '$2y$10$uKz.LBrHR9Dwf/qTjsDvfe6t1HDTtUpHy9LfsCXPnKzrA0rXzfQB6', 'user'),
(2, 'admin', 'admin@example.com', '$2y$10$JMrGzf4Uu1ZfvUw2zRz3Z.Zr9PpTuB54QXxA9wH4P0uHaAeDS.i5y', 'admin'),
(3, 'landlord', 'landlord@example.com', '$2y$10$uKz.LBrHR9Dwf/qTjsDvfe6t1HDTtUpHy9LfsCXPnKzrA0rXzfQB6', 'landlord');

-- Insert test landlords
INSERT INTO landlords (id, user_id, name, contact_number, address) VALUES
(1, 3, 'John Smith Properties', '123-456-7890', '123 Landlord St, Real Estate City, RE 12345');

-- Insert test properties
INSERT INTO properties (id, landlord_id, title, description, price, address, city, state, zip, bedrooms, bathrooms, area, property_type, status, featured, image_urls) VALUES
(1, 1, 'Cozy Downtown Apartment', 'A beautiful apartment in the heart of downtown.', 1200.00, '456 Main St', 'Cityville', 'State', '12345', 2, 1.5, 950.50, 'apartment', 'available', 1, 'image1.jpg,image2.jpg'),
(2, 1, 'Spacious Family Home', 'Perfect home for a growing family with a large backyard.', 2500.00, '789 Oak Ave', 'Townburg', 'State', '12346', 4, 2.5, 2200.00, 'house', 'available', 0, 'image3.jpg,image4.jpg'),
(3, 1, 'Modern Studio Apartment', 'Sleek and modern studio apartment with all amenities.', 950.00, '123 Pine St', 'Cityville', 'State', '12345', 1, 1.0, 550.00, 'studio', 'rented', 0, 'image5.jpg');

-- Insert test favorites
INSERT INTO favorites (id, user_id, property_id) VALUES
(1, 1, 1),
(2, 1, 2);

-- Insert test requests
INSERT INTO requests (id, user_id, property_id, message, status, viewing_date) VALUES
(1, 1, 1, 'I would like to view this property on Saturday.', 'pending', '2025-03-01 14:00:00'),
(2, 1, 2, 'Is this property pet-friendly?', 'approved', '2025-03-02 10:30:00'),
(3, 1, 3, 'When will this property be available again?', 'rejected', NULL);
