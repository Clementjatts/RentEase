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

-- Insert default admin account
INSERT OR IGNORE INTO users (username, password, email, user_type, full_name, phone)
VALUES ('admin', 'pass', 'admin@rentease.com', 'ADMIN', 'Admin User', '+1234567890');
