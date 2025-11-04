ALTER TABLE appointment
    ADD COLUMN appointment_type_id UUID,
ADD CONSTRAINT fk_appointment_type
    FOREIGN KEY (appointment_type_id) REFERENCES appointment_type(id);