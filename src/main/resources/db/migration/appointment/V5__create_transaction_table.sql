CREATE TABLE transaction (
                             id SERIAL PRIMARY KEY,
                             amount_subtotal BIGINT,
                             amount_total BIGINT,
                             currency VARCHAR(10),
                             payment_status VARCHAR(50),
                             stripe_status VARCHAR(50),
                             payment_transaction_id VARCHAR(255),
                             payment_method_type VARCHAR(50),
                             livemode BOOLEAN,
                             mode VARCHAR(50),
                             customer_details JSONB,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             appointment_id BIGINT UNIQUE REFERENCES appointment(id)
);