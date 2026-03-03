CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER'
);

INSERT INTO users (full_name, email, password, role) 
VALUES ('Sanjeev PM', 'admin@msr.com', 'admin123', 'ADMIN');