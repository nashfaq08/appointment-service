-- V2__create_appointment_type_table.sql

CREATE TABLE appointment_type (
                                  id SERIAL PRIMARY KEY,
                                  name VARCHAR(100) UNIQUE NOT NULL
);
