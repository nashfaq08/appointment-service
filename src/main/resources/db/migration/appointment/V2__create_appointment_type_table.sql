-- V2__create_appointment_type_table.sql
CREATE TABLE appointment_type (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  name VARCHAR(100) UNIQUE NOT NULL
);
