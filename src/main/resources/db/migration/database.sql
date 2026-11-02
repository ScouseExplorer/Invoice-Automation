CREATE TABLE customer(
    customer_id SERIAL PRIMARY KEY,
    customer_name VARCHAR(50),
    customer_email VARCHAR(100),
    customer_phone_num VARCHAR(11),
    customer_invoice_amount DECIMAL(10, 2) NOT NULL
);

CREATE TABLE invoice(
    invoice_id SERIAL PRIMARY KEY,
    invoice_amount DECIMAL(10, 2) NOT NULL,
    invoice_number VARCHAR(50),
    customer_id INT REFERENCES customer(customer_id) NOT NULL,
    customer_name VARCHAR(50),
    invoice_date DATE NOT NULL,
    invoice_due_date DATE NOT NULL,
    invoice_issued_date DATE NOT NULL,
    invoice_status VARCHAR(20) NOT NULL,
    invoice_payment_date DATE,
    invoice_created_At TIMESTAMP NOT NULL
    
);