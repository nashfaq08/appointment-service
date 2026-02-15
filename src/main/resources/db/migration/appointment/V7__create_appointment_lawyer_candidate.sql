CREATE TABLE appointment_lawyer_candidate (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                              appointment_id UUID NOT NULL,
                                              lawyer_id UUID NOT NULL,
                                              status VARCHAR(20) NOT NULL,
                                              notified_at TIMESTAMP DEFAULT now(),

                                              CONSTRAINT fk_candidate_appointment
                                                  FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE,

                                              CONSTRAINT uq_appointment_lawyer UNIQUE (appointment_id, lawyer_id)
);

CREATE INDEX idx_candidate_lawyer_id ON appointment_lawyer_candidate(lawyer_id);
CREATE INDEX idx_candidate_status ON appointment_lawyer_candidate(status);