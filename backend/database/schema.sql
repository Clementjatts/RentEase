-- SIMPLIFIED SCHEMA - ADMIN and LANDLORD only per CSYM030.md
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    email TEXT NOT NULL,
    phone TEXT,
    user_type TEXT NOT NULL CHECK (user_type IN ('ADMIN', 'LANDLORD')),
    full_name TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS properties (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    price REAL NOT NULL,
    bedroom_count INTEGER,
    bathroom_count INTEGER,
    furniture_type TEXT,
    address TEXT,
    landlord_id INTEGER NOT NULL, -- Direct reference to users table
    image_url TEXT, -- Single image URL field
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (landlord_id) REFERENCES users(id)
);

-- Requests table for storing contact requests/notifications
CREATE TABLE IF NOT EXISTS requests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    property_id INTEGER NOT NULL,
    landlord_id INTEGER NOT NULL,
    requester_name TEXT NOT NULL,
    requester_email TEXT NOT NULL,
    requester_phone TEXT,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    FOREIGN KEY (landlord_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_requests_landlord_id ON requests(landlord_id);
CREATE INDEX IF NOT EXISTS idx_requests_property_id ON requests(property_id);
CREATE INDEX IF NOT EXISTS idx_requests_is_read ON requests(is_read);

-- Insert default admin account with hashed password
-- Password: 'password' hashed using PHP password_hash()
INSERT OR IGNORE INTO users (username, password, email, user_type, full_name, phone)
VALUES ('admin', '$2y$12$OnXnF4RzoYwE3pY4vg6n1upqGaTRLbvyIBmikY.Fq.sNRgZDwkIF6', 'admin@rentease.com', 'ADMIN', 'Admin User', '+1234567890');
