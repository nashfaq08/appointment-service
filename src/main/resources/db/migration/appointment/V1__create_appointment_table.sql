CREATE TABLE appointment (
             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

             customer_id UUID NOT NULL,
             lawyer_id UUID NOT NULL,

             appointment_date DATE NOT NULL,
             start_time TIME NOT NULL,
             end_time TIME NOT NULL,
             status VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
             description TEXT,

             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
